# -*- coding: utf-8 -*-
"""
faq_pipeline_main.py  ―  1:1 문의 → FAQ 후보 전체 파이프라인 묶음 (진입점)

이 파일이 하는 일
    네 단계를 순서대로 이어 붙여, '버튼 한 번'으로 도는 진입 함수를 만든다.

        1) p1_fetch_inquiries()        DB에서 문의 읽기
        2) p2_cluster_inquiries()      의미별 클러스터링
        3) p3_classify_against_faq()   FAQ 대조 → 있음/신규 분류
        4) p4_save_new_candidates()    신규 후보만 DB 저장

    단계 사이는 '파일'이 아니라 '메모리(리턴값)'로 넘긴다.

위치
    이 파일만 app/services/ 바로 아래에 둔다(라우터/서비스에서 부르기 쉬운 진입점).
    1~4단계는 app/services/faq_pipeline/ 하위 패키지에 있다.

진입 함수
    refresh_faq_candidates()  ―  FastAPI 엔드포인트가 이 함수 하나만 호출하면 된다.
                                 스프링 'FAQ 후보 갱신' 버튼이 결국 부르는 대상.

진행 로그
    각 단계 시작을 [1/4] 식으로 터미널에 즉시(flush) 찍는다.
    스프링 화면은 빙글빙글 로딩만 보이고, 자세한 진행은 이 터미널에서 본다.
"""

import time

from app.services.faq_pipeline.p1_load_inquiries import fetch_inquiries
from app.services.faq_pipeline.p2_cluster_inquiries import cluster_inquiries
from app.services.faq_pipeline.p3_analyze_gaps import classify_against_faq
from app.services.faq_pipeline.p4_register_candidates import save_new_candidates


def _log(step: str, msg: str) -> None:
    """진행 상황을 터미널에 즉시 출력한다(uvicorn 버퍼링 방지로 flush=True)."""
    print(f"\n[{step}] {msg}", flush=True)


def refresh_faq_candidates(min_cluster_size: int = 5) -> dict:
    """
    1:1 문의를 분석해 신규 FAQ 후보를 DB에 등록하는 전체 파이프라인.

    동작:
        DB 문의 읽기 → 클러스터링 → FAQ 대조 → 신규 후보 저장 을 한 번에 수행한다.
        각 단계 결과는 메모리로 다음 단계에 넘긴다(중간 파일 없음).

    매개변수:
        min_cluster_size : 공통 문의로 인정할 최소 건수(클러스터링 민감도).

    반환:
        dict ― 실행 요약. FastAPI 응답으로 그대로 쓸 수 있다. 예)
            {
              "ok": True,
              "inquiries": 213,       # 분석한 문의 수
              "clusters": 14,         # 만들어진 클러스터 수
              "new_candidates": 5,    # 이번에 새로 등록된 신규 후보 수
              "elapsed_sec": 18.4,    # 총 소요 시간
            }

    참고:
        - 무거운 작업(임베딩 등)이라 수십 초 걸릴 수 있다(동기 실행).
        - 중복 방어는 4단계(save_new_candidates)에 있다. 한 사이클당 1회 호출을 전제로 한다.
    """
    t0 = time.time()

    _log("1/4", "DB에서 문의 읽는 중...")
    df = fetch_inquiries()
    print(f"      문의 {len(df)}건 로드", flush=True)

    _log("2/4", "의미별 클러스터링 중...")
    clusters = cluster_inquiries(df, min_cluster_size=min_cluster_size)

    _log("3/4", "기존 FAQ와 대조 중...")
    results = classify_against_faq(clusters)

    _log("4/4", "신규 후보 DB 저장 중...")
    save_summary = save_new_candidates(results)

    elapsed = round(time.time() - t0, 1)
    summary = {
        "ok": True,
        "inquiries": int(len(df)),
        "clusters": len(clusters),
        "new_candidates": save_summary.get("inserted", 0),
        "elapsed_sec": elapsed,
    }

    _log("완료", f"신규 후보 {summary['new_candidates']}건 등록 / "
                 f"총 {elapsed}초")
    return summary

# 서비스 클래스로 감싸기
class FaqPipelineService:

    # refresh_candidates 함수를 서비스 객체로 호출하기 위한 클래스 -> 코딩 스타일 통합용
    def refresh_candidates(self, min_cluster_size: int = 5) -> dict:
        return refresh_faq_candidates(min_cluster_size=min_cluster_size)


if __name__ == "__main__":
    # ── 단독 실행: 전체 파이프라인을 한 번 돌려보고 요약 출력 ──
    #    python -m app.services.faq_pipeline_main
    result = refresh_faq_candidates()
    print("\n=== 실행 요약 ===", flush=True)
    print(result, flush=True)
