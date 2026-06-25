# -*- coding: utf-8 -*-
"""
register_candidates.py  ―  [파이프라인 4단계] 신규 후보를 DB에 저장 (+중복 방어)

이 파일이 하는 일
    classify_against_faq() 결과 중 verdict == "신규후보" 인 것을
    TB_FAQ_CANDIDATE 에 저장한다. 단, 이미 큐에 있는 주제는 새로 넣지 않는다.

중복 방어 (이 파일의 핵심)
    새 후보를 넣기 전에, DB의 기존 후보(대기/반려)들과 임베딩 유사도를 비교한다.
        - 비슷한 '대기' 후보가 있으면 → 새로 안 넣고 그 후보의 INQUIRY_COUNT 만 누적
        - 비슷한 '반려' 후보가 있으면 → 무시(안 만들기로 한 주제니까 다시 안 올림)
        - 비슷한 게 없으면          → 새 후보로 INSERT
    ('승인'된 후보는 FAQ에 등록됐을 테니 3단계에서 이미 "있음"으로 걸러진다.
     그래서 후보 테이블에선 '대기'/'반려'만 비교 대상으로 본다.)

    비교는 '대표 문장 ↔ 대표 문장'(둘 다 구어체)이라 같은 주제면 유사도가 높게(0.5~0.85)
    나온다. → DUP_THRESHOLD 를 0.6 으로 둔다. (FAQ 대조 임계값 0.0 과는 다른 값)

저장하는 값
    실제 DB에 저장하는 단계 
    REP_QUESTION / CATEGORY / INQUIRY_COUNT / SIMILARITY / STATUS('대기') / EMBEDDING(JSON)

진입 함수
    save_new_candidates()  ― refresh_faq_candidates() 가 마지막에 호출하는 단계.

이식 메모(경로/설정)
    - .env 로딩·OpenAI 키·모델은 app.core.config 에서 가져온다.
    - DB 연결은 1단계의 _connect() 를 재사용한다(DSN 조립 로직 단일화).
    - 단독 실행 입력 파일(gap_result.json)은 파이프라인 캐시 폴더에서 읽는다.
"""

import json
from pathlib import Path

import numpy as np
import oracledb
from openai import OpenAI

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL
from app.services.faq_pipeline.p1_load_inquiries import _connect  # DB 연결 단일 정의 재사용

# EMBEDDING(CLOB)을 LOB 객체가 아니라 문자열로 받아 json.loads 하기 위함
oracledb.defaults.fetch_lobs = False

EMBED_MODEL = OPENAI_EMBED_MODEL or "text-embedding-3-small"

HERE = Path(__file__).resolve().parent
CACHE_DIR = BASE_DIR / "vector_stores" / "pipeline_cache"
GAP_PATH = CACHE_DIR / "gap_result.json"   # 단독 실행 시 입력으로 읽는 파일
DUP_THRESHOLD = 0.6                          # 같은 주제로 볼 후보 간 유사도 기준

client = OpenAI(api_key=OPENAI_API_KEY)

INSERT_SQL = """
INSERT INTO TB_FAQ_CANDIDATE
    (REP_QUESTION, CATEGORY, INQUIRY_COUNT, SIMILARITY, STATUS, EMBEDDING)
VALUES
    (:rep, :cat, :cnt, :sim, '대기', :emb)
RETURNING CANDIDATE_NO INTO :new_no
"""

BUMP_SQL = """
UPDATE TB_FAQ_CANDIDATE
   SET INQUIRY_COUNT = INQUIRY_COUNT + :cnt,
       UPDATED_AT = SYSDATE
 WHERE CANDIDATE_NO = :no
"""


def _cosine(a, b) -> float:
    """두 벡터의 코사인 유사도. 한쪽이 0벡터면 -1.0 을 돌려 매칭에서 빠지게 한다."""
    a = np.asarray(a, dtype=np.float32)
    b = np.asarray(b, dtype=np.float32)
    na, nb = np.linalg.norm(a), np.linalg.norm(b)
    if na == 0 or nb == 0:
        return -1.0
    return float(a @ b / (na * nb))


def _best_match(vec, existing: list[dict]):
    """
    vec 과 가장 비슷한 기존 후보를 찾는다.

    매개변수:
        vec      : 새 후보의 임베딩
        existing : [{"no":.., "status":.., "vec":..}, ...] 기존 대기/반려 후보들
    반환:
        (match, sim) ― 가장 비슷한 후보 dict 와 그 유사도. existing 이 비면 (None, -1.0).
    """
    best, best_sim = None, -1.0
    for e in existing:
        s = _cosine(vec, e["vec"])
        if s > best_sim:
            best, best_sim = e, s
    return best, best_sim


def save_new_candidates(results: list[dict] | None = None) -> dict:
    """
    [파이프라인 4단계] 신규 후보를 TB_FAQ_CANDIDATE 에 저장한다. (중복 방어 포함)

    동작:
        1) results 에서 verdict == "신규후보" 만 추린다.
        2) 대표 문의를 임베딩한다.
        3) DB의 기존 '대기'/'반려' 후보 임베딩을 읽어둔다.
        4) 새 후보마다 가장 비슷한 기존 후보를 찾아:
             - 대기 + 유사도≥DUP_THRESHOLD → 건수 누적(BUMP), INSERT 안 함
             - 반려 + 유사도≥DUP_THRESHOLD → 무시
             - 그 외                        → INSERT (그리고 같은 실행 내 비교 대상에 추가)

    매개변수:
        results : classify_against_faq() 반환값. None 이면 gap_result.json 을 읽는다.

    반환:
        dict ― {"inserted":신규INSERT수, "accumulated":누적수, "ignored":반려무시수,
                "before":처리전행수, "after":처리후행수}
    """
    if results is None:
        results = json.load(open(GAP_PATH, encoding="utf-8"))

    candidates = [r for r in results if r.get("verdict") == "신규후보"]
    if not candidates:
        print("신규 후보가 없습니다.")
        return {"inserted": 0, "accumulated": 0, "ignored": 0,
                "before": None, "after": None}

    print(f"신규 후보 {len(candidates)}건 검토 시작 (중복 기준 {DUP_THRESHOLD})\n")

    # 새 후보 대표 문의 임베딩
    reps = [c["representative"] for c in candidates]
    resp = client.embeddings.create(model=EMBED_MODEL, input=reps)
    new_embs = [d.embedding for d in resp.data]

    inserted = accumulated = ignored = 0

    with _connect() as conn:
        with conn.cursor() as cur:
            before = cur.execute("SELECT COUNT(*) FROM TB_FAQ_CANDIDATE").fetchone()[0]

            # 기존 대기/반려 후보 임베딩 적재(중복 비교 대상)
            cur.execute(
                "SELECT CANDIDATE_NO, STATUS, EMBEDDING "
                "FROM TB_FAQ_CANDIDATE WHERE STATUS IN ('대기', '반려')"
            )
            existing = [
                {"no": no, "status": status, "vec": json.loads(emb)}
                for no, status, emb in cur.fetchall()
            ]

            for c, emb in zip(candidates, new_embs):
                match, sim = _best_match(emb, existing)
                label = f"[{c.get('size', 0):2d}건] [{c.get('dominant_category')}] " \
                        f"{c['representative'][:32]}"

                # ── 중복: 비슷한 기존 후보가 있는 경우 ──
                if match and sim >= DUP_THRESHOLD:
                    if match["status"] == "대기":
                        cur.execute(BUMP_SQL, {"cnt": c.get("size", 0), "no": match["no"]})
                        accumulated += 1
                        print(f"  ↑ 누적  {label}  (기존 #{match['no']}, 유사도 {sim:.2f})")
                    else:  # 반려
                        ignored += 1
                        print(f"  · 무시  {label}  (반려된 주제, 유사도 {sim:.2f})")
                    continue

                # ── 신규: INSERT ──
                new_no = cur.var(oracledb.DB_TYPE_NUMBER)
                cur.setinputsizes(emb=oracledb.DB_TYPE_CLOB)   # 큰 JSON → CLOB 바인딩
                cur.execute(INSERT_SQL, {
                    "rep": c["representative"][:500],
                    "cat": c.get("dominant_category"),
                    "cnt": c.get("size", 0),
                    "sim": c.get("similarity"),
                    "emb": json.dumps(emb),
                    "new_no": new_no,
                })
                # 같은 실행 안에서 뒤따르는 후보가 이걸 또 중복으로 잡을 수 있게 추가
                existing.append({
                    "no": new_no.getvalue()[0], "status": "대기", "vec": emb,
                })
                inserted += 1
                print(f"  + 등록  {label}")

            conn.commit()
            after = cur.execute("SELECT COUNT(*) FROM TB_FAQ_CANDIDATE").fetchone()[0]

    print(f"\n완료. 등록 {inserted} / 누적 {accumulated} / 무시 {ignored}  "
          f"| TB_FAQ_CANDIDATE: {before} → {after}건")
    return {"inserted": inserted, "accumulated": accumulated, "ignored": ignored,
            "before": before, "after": after}


if __name__ == "__main__":
    # ── 단독 실행: gap_result.json 을 읽어 신규 후보를 DB에 등록 ──
    #    python -m app.services.faq_pipeline.register_candidates
    save_new_candidates()
