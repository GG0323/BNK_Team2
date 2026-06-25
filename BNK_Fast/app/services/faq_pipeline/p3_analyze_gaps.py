# -*- coding: utf-8 -*-
"""
analyze_gaps.py  ―  [파이프라인 3단계] 클러스터를 기존 FAQ와 대조해 분류

이 파일이 하는 일
    cluster_inquiries() 가 만든 클러스터 각각을, 기존 FAQ(chroma 'faqs')와 비교한다.
    가장 비슷한 FAQ 와의 유사도가
        임계값 이상 → "FAQ있음"  (이미 답이 있는 자주 묻는 질문)
        임계값 미만 → "신규후보"  (FAQ에 없는 주제 → 검수 큐로)

중요 (벡터 공간 일치)
    FAQ DB는 text-embedding-3-small 로 '벡터를 직접' 넣었다(embedding_function 없음).
    그래서 검색도 query_embeddings 로 넘겨야 같은 벡터 공간에서 비교된다.
    (query_texts 로 넘기면 chroma 기본 모델로 임베딩돼 공간이 어긋난다.)
    → 팀원의 FaqAdminService/FaqLlmService 도 동일하게 벡터를 직접 넣고
      query_embeddings 로 검색하므로 공간이 일치한다.

진입 함수
    classify_against_faq()    ― 파이프라인 본체. 분류 결과를 반환.
    evaluate_classification() ― 더미 정답표 채점(개발 검증용). 운영 경로엔 호출 안 함.

이식 메모(경로/설정)
    - 3번 파이프라인에 백터 DB를 바라보는 경로가 존재한다.
    - FAQ 벡터DB 경로를 팀원 실제 경로(vector_stores/chroma_faq_db)로 맞춤.
    - .env 로딩·OpenAI 키·모델은 app.core.config 에서 가져온다.
"""

import json
from pathlib import Path

import chromadb
from openai import OpenAI

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL
from app.services.faq_pipeline.p1_load_inquiries import fetch_inquiries  # 평가에서 순서 확인용

HERE = Path(__file__).resolve().parent

EMBED_MODEL = OPENAI_EMBED_MODEL or "text-embedding-3-small"
# 팀원 코드(FaqAdminService/FaqLlmService)와 동일한 FAQ 벡터DB 를 본다.
CHROMA_PATH = str(BASE_DIR / "vector_stores" / "chroma_faq_db")
FAQ_COLLECTION_NAME = "faqs"

GT_PATH = HERE / "dummy_ground_truth.json"
CACHE_DIR = BASE_DIR / "vector_stores" / "pipeline_cache"
CACHE_DIR.mkdir(parents=True, exist_ok=True)

# 분류 기준(코사인 유사도). 더미 정답표 탐색에서 -0.14~+0.04 구간이 100%였고,
# 가운데값인 0.0 이 가장 안전한 여유값이라 채택.
THRESHOLD = 0.0

client = OpenAI(api_key=OPENAI_API_KEY)


def _embed(texts: list[str]) -> list[list[float]]:
    """
    텍스트 목록을 임베딩한다. (대표 문의 몇 개만 다루므로 캐싱 없음)

    매개변수:
        texts : 임베딩할 문자열 리스트(보통 클러스터 대표 문의들)
    반환:
        list[list[float]] ― 각 텍스트의 임베딩 벡터
    """
    resp = client.embeddings.create(model=EMBED_MODEL, input=texts)
    return [d.embedding for d in resp.data]


def classify_against_faq(
    clusters: list[dict],
    threshold: float = THRESHOLD,
    save_json: str | None = None,
) -> list[dict]:
    """
    [파이프라인 3단계] 각 클러스터를 기존 FAQ와 대조해 '있음/신규'로 분류한다.

    동작:
        1) 클러스터 대표 문의들을 임베딩한다.
        2) chroma 'faqs' 컬렉션에서 가장 비슷한 FAQ 1건을 찾는다.
        3) 유사도가 threshold 이상이면 "FAQ있음", 미만이면 "신규후보".

    매개변수:
        clusters  : cluster_inquiries() 가 반환한 클러스터 목록.
        threshold : 있음/신규를 가르는 코사인 유사도 기준(기본 THRESHOLD).
        save_json : 디버깅용. 경로를 주면 분류 결과를 JSON 으로도 저장.

    반환:
        list[dict] ― 유사도 높은 순 정렬. 각 원소:
            {
              "cluster_id", "size", "representative", "dominant_category",
              "similarity"        : 최근접 FAQ 와의 코사인 유사도,
              "nearest_faq"       : 가장 비슷한 FAQ 질문,
              "nearest_faq_answer": 그 FAQ 답변,
              "inquiry_nos"       : 클러스터 구성 문의 번호들,
              "verdict"           : "FAQ있음" 또는 "신규후보",
            }

    다음 단계:
        이 결과 중 verdict == "신규후보" 인 것을 save_new_candidates() 가 DB에 넣는다.
    """
    col = chromadb.PersistentClient(path=CHROMA_PATH).get_collection(FAQ_COLLECTION_NAME)

    # 대표 문의들을 한 번에 임베딩 → FAQ 검색
    reps = [c["representative"] for c in clusters]
    rep_vecs = _embed(reps)

    results = []
    for c, vec in zip(clusters, rep_vecs):
        res = col.query(query_embeddings=[vec], n_results=1)
        similarity = 1 - res["distances"][0][0]      # chroma 코사인 거리 → 유사도
        results.append({
            "cluster_id": c["cluster_id"],
            "size": c["size"],
            "representative": c["representative"],
            "dominant_category": c["dominant_category"],
            "similarity": round(float(similarity), 3),
            "nearest_faq": res["documents"][0][0],
            "nearest_faq_answer": res["metadatas"][0][0]["answer"],
            "inquiry_nos": c["inquiry_nos"],
        })

    # 유사도 높은 순 정렬(있음 후보가 위로) + verdict 부여
    results.sort(key=lambda r: -r["similarity"])
    for r in results:
        r["verdict"] = "FAQ있음" if r["similarity"] >= threshold else "신규후보"

    # ── 분류 결과 출력 ──
    faq_hits = [r for r in results if r["verdict"] == "FAQ있음"]
    new_cands = [r for r in results if r["verdict"] == "신규후보"]

    print(f"=== 분류 결과 (임계값 {threshold}) ===\n")
    print("[FAQ 있음 — 자주 묻는 질문]")
    for r in faq_hits:
        print(f"  [{r['size']:2d}건] {r['representative'][:35]}")
        print(f"        ↳ 매칭 FAQ: {r['nearest_faq'][:40]} (유사도 {r['similarity']})")
    print("\n[신규 후보 — 검수 큐로]")
    for r in new_cands:
        print(f"  [{r['size']:2d}건] [{r['dominant_category']}] "
              f"{r['representative'][:35]} (최근접 유사도 {r['similarity']})")
    print(f"\n분류 완료: FAQ있음 {len(faq_hits)} / 신규후보 {len(new_cands)}")

    if save_json:
        json.dump(results, open(save_json, "w", encoding="utf-8"),
                  ensure_ascii=False, indent=2)
        print(f"(디버그 저장) {save_json}")

    return results


def _attach_cluster_truth(results: list[dict], df, gt: list[dict]) -> None:
    """
    [개발 검증용 헬퍼] 각 클러스터의 '정답'(in_faq/intent)을 구성 문의 다수결로 산출해
    results 의 각 원소에 _true_in_faq / _true_intent 로 붙인다.

    근거:
        정답표(gt)는 seq_index 순서 = INQUIRY_NO 오름차순. df 도 같은 순서이므로
        i번째 gt 가 i번째 INQUIRY_NO 에 대응한다. 이를 inquiry_no→정답 사전으로 만든 뒤,
        클러스터에 속한 문의들의 다수결로 그 클러스터의 정답을 정한다.
    """
    inq_order = df["INQUIRY_NO"].tolist()                  # 오름차순
    no_to_infaq = {inq_order[i]: gt[i]["in_faq"] for i in range(len(gt))}
    no_to_intent = {inq_order[i]: gt[i]["intent"] for i in range(len(gt))}

    for r in results:
        votes = [no_to_infaq.get(n) for n in r["inquiry_nos"] if n in no_to_infaq]
        r["_true_in_faq"] = (sum(votes) > len(votes) / 2) if votes else None
        intents = [no_to_intent.get(n) for n in r["inquiry_nos"] if n in no_to_intent]
        r["_true_intent"] = max(set(intents), key=intents.count) if intents else None


def evaluate_classification(results: list[dict], df, gt_path: Path = GT_PATH) -> None:
    """
    [개발 검증용] 분류 결과를 더미 정답표와 대조한다.

    ※ 더미 데이터일 때만 의미가 있다. 운영 문의엔 정답표가 없으므로 호출하지 않는다.

    출력:
        1) 임계값(-0.30~0.40)별 분류 정확도와 최적 임계값
        2) 현재 THRESHOLD 기준 오분류(❌) 점검

    매개변수:
        results : classify_against_faq() 의 반환값(verdict 포함)
        df      : fetch_inquiries() 결과(INQUIRY_NO 순서 확인용)
        gt_path : 정답표(dummy_ground_truth.json) 경로
    """
    if not gt_path.exists():
        print(f"\n(정답표 {gt_path.name} 없음 — 평가 생략)")
        return
    gt = json.load(open(gt_path, encoding="utf-8"))
    if len(gt) != len(df):
        print(f"\n⚠️ 정답표({len(gt)})와 문의수({len(df)}) 불일치 — 평가 생략.")
        return

    _attach_cluster_truth(results, df, gt)

    # 1) 임계값별 정확도 탐색
    print("\n=== 임계값별 정확도 (클러스터 분류 기준) ===")
    best_t, best_acc = None, -1.0
    for t in [x / 100 for x in range(-30, 41, 2)]:
        evaluable = [r for r in results if r["_true_in_faq"] is not None]
        correct = sum(1 for r in evaluable
                      if (r["similarity"] >= t) == r["_true_in_faq"])
        acc = correct / len(evaluable) if evaluable else 0
        if acc > best_acc:
            best_acc, best_t = acc, t
        print(f"  임계값 {t:.2f} → {correct}/{len(evaluable)} ({acc*100:.0f}%)")
    print(f"\n>>> 최적 임계값: {best_t:.2f} (정확도 {best_acc*100:.0f}%)")
    print(f"    (현재 코드 THRESHOLD={THRESHOLD})")

    # 2) 현재 THRESHOLD 기준 오분류 점검
    mismatches = [
        r for r in results
        if r["_true_in_faq"] is not None
        and (r["verdict"] == "FAQ있음") != r["_true_in_faq"]
    ]
    print(f"\n[오분류 점검 (임계값 {THRESHOLD} 기준)]")
    if not mismatches:
        print("  오분류 없음 ✅")
    else:
        for r in mismatches:
            why = ("실제로는 신규인데 있음으로 분류" if r["verdict"] == "FAQ있음"
                   else "실제로는 FAQ에 있는데 신규로 분류")
            print(f"  ❌ [{r['size']:2d}건] {r['representative'][:30]} — {why}")


if __name__ == "__main__":
    # ── 단독 실행: 추출 → 클러스터링 → FAQ 대조 → 더미 채점 (파이프라인 미리보기) ──
    #    python -m app.services.faq_pipeline.analyze_gaps
    from app.services.faq_pipeline.p2_cluster_inquiries import cluster_inquiries
    df = fetch_inquiries()
    clusters = cluster_inquiries(df)
    results = classify_against_faq(clusters, save_json=str(CACHE_DIR / "gap_result.json"))
    evaluate_classification(results, df)
