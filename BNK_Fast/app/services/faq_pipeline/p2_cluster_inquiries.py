# -*- coding: utf-8 -*-
"""
cluster_inquiries.py  ―  [파이프라인 2단계] 문의를 의미별로 묶기

이 파일이 하는 일
    fetch_inquiries() 가 준 문의들을 임베딩하여, 의미가 비슷한 것끼리
    자동으로 묶는다(클러스터링). 이 묶음 하나하나가 '공통 문의 주제'다.

흐름
    문의 텍스트 → OpenAI 임베딩 → L2 정규화 → HDBSCAN → 클러스터 목록

핵심 파라미터
    MIN_CLUSTER_SIZE : "몇 건 이상 모여야 공통 문의로 칠 거냐" = 비즈니스 기준 그 자체.
                       올리면 큰 묶음만, 내리면 작은 묶음까지 잡는다.

진입 함수
    cluster_inquiries()   ― 파이프라인이 쓰는 본체. 클러스터 목록을 반환.
    evaluate_clustering() ― 더미 정답표 채점(개발 검증용). 운영 경로에선 호출 안 함.

이식 메모(경로/설정)
    - .env 로딩·OpenAI 키는 app.core.config 에서 가져온다(직접 load_dotenv 안 함).
    - 임베딩 캐시(.npy)는 소스 폴더가 아니라 BASE_DIR/vector_stores/pipeline_cache 에
      쓴다. (배포 시 소스 폴더가 읽기전용일 수 있어 격리)

설치
    pip install scikit-learn numpy
"""

import json
import hashlib
from pathlib import Path

import numpy as np
from openai import OpenAI
from sklearn.cluster import HDBSCAN
from sklearn.preprocessing import normalize

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL
from app.services.faq_pipeline.p1_load_inquiries import fetch_inquiries  # ← 1단계 함수

MIN_CLUSTER_SIZE = 5
EMBED_MODEL = OPENAI_EMBED_MODEL or "text-embedding-3-small"

HERE = Path(__file__).resolve().parent
GT_PATH = HERE / "dummy_ground_truth.json"            # 더미 정답표(개발 검증용, 읽기)

# 쓰기가 필요한 산출물(임베딩 캐시 등)은 소스 폴더 밖으로 격리한다.
CACHE_DIR = BASE_DIR / "vector_stores" / "pipeline_cache"
CACHE_DIR.mkdir(parents=True, exist_ok=True)

client = OpenAI(api_key=OPENAI_API_KEY)


def embed_texts(texts: list[str]) -> np.ndarray:
    """
    텍스트 목록을 임베딩 벡터로 변환한다. (내용 해시 기반 캐싱 포함)

    캐싱 이유:
        같은 문의 집합을 MIN_CLUSTER_SIZE 만 바꿔가며 여러 번 돌릴 때
        매번 OpenAI 를 다시 부르면 돈·시간이 낭비된다. 텍스트 내용으로
        해시 키를 만들어, 내용이 같으면 디스크 캐시(.npy)를 재사용한다.
        (문의가 한 건이라도 바뀌면 해시가 달라져 자동으로 새로 생성한다.)

    매개변수:
        texts : 임베딩할 문자열 리스트
    반환:
        np.ndarray (shape = [문의수, 1536]) ― float32 임베딩 행렬
    """
    key = hashlib.md5("\n".join(texts).encode("utf-8")).hexdigest()[:12]
    cache = CACHE_DIR / f"emb_cache_{key}.npy"
    if cache.exists():
        print(f"임베딩 캐시 사용: {cache.name}")
        return np.load(cache)

    print(f"임베딩 생성 중... ({len(texts)}건)")
    vecs = []
    for i in range(0, len(texts), 100):           # 100개씩 끊어서 호출
        chunk = texts[i:i + 100]
        resp = client.embeddings.create(model=EMBED_MODEL, input=chunk)
        vecs.extend([d.embedding for d in resp.data])
    embs = np.array(vecs, dtype=np.float32)
    np.save(cache, embs)
    return embs


def cluster_inquiries(
    df=None,
    min_cluster_size: int = MIN_CLUSTER_SIZE,
    save_json: str | None = None,
) -> list[dict]:
    """
    [파이프라인 2단계] 문의를 의미별로 묶어 '클러스터 목록'을 반환한다.

    동작:
        1) 문의 텍스트(df["TEXT"])를 임베딩한다.
        2) L2 정규화한다 → 유클리드 거리가 코사인 유사도와 같은 효과가 된다.
        3) HDBSCAN 으로 묶는다. 비슷한 게 min_cluster_size 미만이면
           묶지 않고 노이즈(-1, = 1회성 문의)로 둔다.
        4) 클러스터마다 대표 문의(중심에 가장 가까운 것)·크기·주 카테고리를 뽑는다.

    매개변수:
        df               : fetch_inquiries() 가 준 DataFrame.
                           None 이면 이 함수가 직접 fetch_inquiries() 를 호출한다.
        min_cluster_size : 공통 문의로 인정할 최소 건수(기본 5).
        save_json        : 디버깅용. 경로를 주면 클러스터 목록을 JSON 으로도 저장.

    반환:
        list[dict] ― 클러스터 목록. 각 원소:
            {
              "cluster_id": int,            # 클러스터 번호
              "size": int,                  # 묶인 문의 수
              "representative": str,        # 대표 문의 텍스트
              "dominant_category": str,     # 주 카테고리
              "inquiry_nos": list[int],     # 이 클러스터에 속한 INQUIRY_NO 들
            }
        ※ 부수효과: 넘겨받은 df 에 "cluster" 컬럼(각 행의 라벨)을 추가한다.

    다음 단계:
        이 목록을 classify_against_faq() 에 넘겨 FAQ 와 대조한다.
    """
    if df is None:
        df = fetch_inquiries()

    texts = df["TEXT"].tolist()
    embs = embed_texts(texts)
    X = normalize(embs)   # L2 정규화 → euclidean == cosine 효과

    labels = HDBSCAN(
        min_cluster_size=min_cluster_size,
        metric="euclidean",
    ).fit_predict(X)
    df["cluster"] = labels   # df 에 라벨 부착(평가/디버그에서 사용)

    n_clusters = len(set(labels) - {-1})
    n_noise = int((labels == -1).sum())
    print(f"\n=== 클러스터링 결과 ===")
    print(f"클러스터 {n_clusters}개 / 노이즈 {n_noise}건 (전체 {len(df)}건)\n")

    # ── 클러스터별 대표/요약 추출 ──
    clusters_out = []
    print("[클러스터별 — 크기순]")
    cluster_ids = sorted(set(labels) - {-1}, key=lambda c: -(labels == c).sum())
    for c in cluster_ids:
        idx = np.where(labels == c)[0]
        sub = X[idx]
        centroid = sub.mean(axis=0)
        rep_local = idx[np.argmax(sub @ centroid)]      # 중심에 가장 가까운 문의 = 대표
        rep_text = texts[rep_local]
        dom_cat = df.iloc[idx]["INQUIRY_CATEGORY"].mode().iloc[0]
        inq_nos = df.iloc[idx]["INQUIRY_NO"].tolist()
        print(f"  클러스터 {c:2d} | {len(idx):3d}건 | [{dom_cat}] {rep_text[:45]}")
        clusters_out.append({
            "cluster_id": int(c),
            "size": int(len(idx)),
            "representative": rep_text,
            "dominant_category": dom_cat,
            "inquiry_nos": [int(x) for x in inq_nos],
        })

    if save_json:
        json.dump(clusters_out, open(save_json, "w", encoding="utf-8"),
                  ensure_ascii=False, indent=2)
        print(f"\n(디버그 저장) {save_json} — 클러스터 {len(clusters_out)}개")

    return clusters_out


def evaluate_clustering(df, gt_path: Path = GT_PATH) -> None:
    """
    [개발 검증용] 클러스터링 결과를 더미 정답표와 대조해 채점한다.

    ※ 이 함수는 '더미 데이터'를 쓸 때만 의미가 있다. 실제 운영 문의에는
       정답표가 없으므로 호출하지 않는다(파이프라인 경로에서 제외).

    채점 항목:
        - 심은 의도별로, 가장 많이 모인 클러스터에 몇 %가 응집했는지
        - 롱테일(1회성)이 노이즈(-1)로 잘 빠졌는지
        - 공통 의도가 잘못 노이즈 처리된 건수

    매개변수:
        df      : cluster_inquiries() 가 "cluster" 컬럼을 붙여둔 DataFrame
        gt_path : 정답표(dummy_ground_truth.json) 경로
    """
    if not gt_path.exists():
        print(f"\n(정답표 {gt_path.name} 없음 — 평가 생략)")
        return

    gt = json.load(open(gt_path, encoding="utf-8"))
    if len(gt) != len(df):
        print(f"\n⚠️ 정답표({len(gt)})와 문의수({len(df)}) 불일치 — "
              f"적재 전 테이블이 비어있지 않았을 수 있음. 평가 생략.")
        return

    # seq_index 순서 = INQUIRY_NO 오름차순 = df 행 순서
    df["true_intent"] = [g["intent"] for g in gt]
    df["bucket"] = [g["bucket"] for g in gt]

    print("\n=== 정답표 대비 평가 ===")
    print("[심은 의도 → 가장 많이 모인 클러스터 / 응집률]")
    for intent in df["true_intent"].unique():
        if intent == "longtail":
            continue
        rows = df[df["true_intent"] == intent]
        non_noise = rows[rows["cluster"] != -1]["cluster"]   # 노이즈 제외
        if len(non_noise) == 0:
            print(f"  {intent:20s} {len(rows):3d}건 → 전부 노이즈 처리됨 ⚠️")
            continue
        top = non_noise.value_counts()
        print(f"  {intent:20s} {len(rows):3d}건 → 클러스터 {top.index[0]} 에 "
              f"{int(top.iloc[0])}건 ({int(top.iloc[0])/len(rows)*100:.0f}%)")

    longtail = df[df["true_intent"] == "longtail"]
    lt_noise = int((longtail["cluster"] == -1).sum())
    common = df[df["bucket"] == "common"]
    common_noise = int((common["cluster"] == -1).sum())
    print("\n[노이즈 처리]")
    print(f"  롱테일 {len(longtail)}건 중 {lt_noise}건 노이즈 처리 "
          f"({lt_noise/len(longtail)*100:.0f}%) — 높을수록 좋음")
    print(f"  공통의도가 잘못 노이즈된 건수: {common_noise}건 — 낮을수록 좋음")


if __name__ == "__main__":
    # ── 단독 실행: 추출 → 클러스터링(클러스터.json 저장) → 더미 채점 ──
    #    python -m app.services.faq_pipeline.cluster_inquiries
    df = fetch_inquiries()
    clusters = cluster_inquiries(df, save_json=str(CACHE_DIR / "clusters.json"))
    evaluate_clustering(df)
