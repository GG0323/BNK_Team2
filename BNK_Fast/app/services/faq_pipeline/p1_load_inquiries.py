# -*- coding: utf-8 -*-
"""
load_inquiries.py  ―  [파이프라인 1단계] DB에서 1:1 문의 읽어오기

이 파일이 하는 일
    Oracle 에서 1:1 문의를 읽어, 클러스터링에 바로 쓸 수 있는 형태(DataFrame)로 만든다.

핵심 규칙
    - 한 문의(INQUIRY_NO)에는 메시지가 여러 개지만, '첫 USER 메시지' 1건만 뽑는다.
    - 클러스터링에 쓸 텍스트 = INQUIRY_TITLE(제목) + 첫 USER 메시지(질문)
    - 직원(ADMIN) 답변은 '고객이 뭘 물었나'와 무관하므로 제외한다.
      (답변 문체가 섞이면 의미 클러스터가 오염된다.)

진입 함수
    fetch_inquiries()  ―  refresh_faq_candidates() 가 가장 먼저 호출하는 단계.

이식 메모(경로/설정)
    - .env 로딩은 app.core.config 가 import 시점에 일괄 처리하므로 여기선 안 한다.
    - DB 접속정보는 config 에서 가져온다. config 에는 통짜 DSN 이 없고
      host/port/service 로 쪼개져 있어, Easy Connect 형식으로 조립해 쓴다.
"""

import oracledb
import pandas as pd

from app.core.config import DB_USER, DB_PW, DB_HOST, DB_PORT, DB_SERVICE_NAME

# CLOB(MSG_CONTENT)을 LOB 객체가 아니라 '문자열'로 바로 받기 위한 설정.
# 이 한 줄이 없으면 본문을 꺼낼 때마다 .read() 를 해줘야 한다.
oracledb.defaults.fetch_lobs = False


# ──────────────────────────────────────────────────────────────────────
# 추출 쿼리
#   각 문의에서 'USER 가 보낸 메시지 중 가장 빠른 것 1건'만 뽑는다.
#   - 서브쿼리에서 ROW_NUMBER() 로 문의별 시간순 번호(RN)를 매기고
#   - 바깥에서 RN = 1 (= 첫 USER 메시지)만 남긴다.
#   - SENDER_TYPE = 'USER' 로 직원(ADMIN) 답변은 처음부터 배제한다.
# ──────────────────────────────────────────────────────────────────────
QUERY = """
SELECT
    i.INQUIRY_NO,
    i.INQUIRY_CATEGORY,
    i.INQUIRY_TITLE,
    i.INQUIRY_STATUS,
    i.CREATED_AT,
    m.MSG_CONTENT AS FIRST_USER_MSG
FROM TB_INQUIRY i
JOIN (
    SELECT
        INQUIRY_NO,
        MSG_CONTENT,
        ROW_NUMBER() OVER (
            PARTITION BY INQUIRY_NO
            ORDER BY MSG_CREATED_AT, MSG_NO
        ) AS RN
    FROM TB_INQUIRY_MSG
    WHERE SENDER_TYPE = 'USER'
) m
  ON m.INQUIRY_NO = i.INQUIRY_NO
 AND m.RN = 1
ORDER BY i.INQUIRY_NO
"""


def _build_dsn() -> str:
    """
    config 의 host/port/service 를 oracledb thin 모드가 바로 먹는
    Easy Connect 문자열(host:port/service_name)로 조립한다. (내부용 헬퍼)

    ※ 셋 중 하나라도 비어 있으면 'None:None/None' 같은 망가진 DSN 이 되어
       연결이 터지므로, 비면 바로 알 수 있게 예외를 던진다.
    """
    if not (DB_HOST and DB_PORT and DB_SERVICE_NAME):
        raise RuntimeError(
            "DB 접속정보 누락: secret.env 의 DB_HOST / DB_PORT / DB_SERVICE_NAME 확인 필요 "
            f"(host={DB_HOST!r}, port={DB_PORT!r}, service={DB_SERVICE_NAME!r})"
        )
    return f"{DB_HOST}:{DB_PORT}/{DB_SERVICE_NAME}"


def _connect():
    """
    Oracle 연결 객체를 생성한다. (내부용 헬퍼)

    접속 정보는 app.core.config 의 DB_USER / DB_PW / host·port·service 에서 읽는다.
    oracledb 는 thin 모드라 Oracle 클라이언트 설치 없이 바로 붙는다.

    반환:
        oracledb.Connection ― 호출 측에서 `with _connect() as conn:` 로 쓴다.
    """
    return oracledb.connect(
        user=DB_USER,
        password=DB_PW,
        dsn=_build_dsn(),
    )


def fetch_inquiries(save_csv: str | None = None) -> pd.DataFrame:
    """
    [파이프라인 1단계] DB의 1:1 문의를 읽어 클러스터링 입력용 DataFrame 으로 반환한다.

    동작:
        1) QUERY 를 실행해 문의별 '첫 USER 메시지'를 한 건씩 가져온다.
        2) 제목 + 첫 질문을 합쳐 TEXT 컬럼을 만든다 (이게 클러스터링의 입력).

    매개변수:
        save_csv : 디버깅용. 경로를 주면 결과를 CSV 로도 저장한다.
                   기본값 None 이면 저장하지 않고 메모리로만 반환한다.
                   (파이프라인은 메모리로 넘기고, 눈으로 보고 싶을 때만 쓴다.)

    반환:
        pd.DataFrame ― 컬럼(전부 대문자):
            INQUIRY_NO, INQUIRY_CATEGORY, INQUIRY_TITLE, INQUIRY_STATUS,
            CREATED_AT, FIRST_USER_MSG, TEXT(=제목+질문)
        INQUIRY_NO 오름차순으로 정렬되어 있다.

    다음 단계:
        이 DataFrame 을 cluster_inquiries() 에 그대로 넘긴다.
    """
    # with 블록을 벗어나면 연결이 자동으로 닫힌다(커서도 함께).
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(QUERY)
            cols = [c[0] for c in cur.description]   # 컬럼명 목록
            rows = cur.fetchall()                    # 전체 행

    df = pd.DataFrame(rows, columns=cols)

    # 클러스터링용 텍스트: 제목 + 첫 질문 (앞뒤 공백 정리)
    df["TEXT"] = (
        df["INQUIRY_TITLE"].fillna("").str.strip()
        + " "
        + df["FIRST_USER_MSG"].fillna("").str.strip()
    ).str.strip()

    if save_csv:
        df.to_csv(save_csv, index=False, encoding="utf-8-sig")
        print(f"(디버그 저장) {save_csv}")

    return df


if __name__ == "__main__":
    # ── 단독 실행 시: 연결·추출이 잘 되는지 눈으로 확인하는 자가진단 ──
    #    패키지 import(app.core.config)를 쓰므로 루트에서 모듈로 실행한다:
    #        python -m app.services.faq_pipeline.load_inquiries
    df = fetch_inquiries()
    print(f"문의 {len(df)}건 로드 완료\n")

    print("[카테고리 분포]")
    print(df["INQUIRY_CATEGORY"].value_counts(), "\n")

    print("[샘플 5건 — 클러스터링에 들어갈 텍스트]")
    for _, r in df.head(5).iterrows():
        print(f"  #{r['INQUIRY_NO']} [{r['INQUIRY_CATEGORY']}] {r['TEXT']}")

    # 첫 USER 메시지가 비어있는 문의 점검(있으면 데이터 이상 신호)
    empty = df[df["FIRST_USER_MSG"].fillna("").str.strip() == ""]
    if len(empty):
        print(f"\nUSER 메시지가 없는 문의 {len(empty)}건 (확인 필요)")
