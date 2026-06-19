import numpy as np
from insightface.app import FaceAnalysis

# 얼굴이 얼마나 비슷한지 점수 매기는 코사인 유사도 함수
# linalg = 선형대수 -> 벡터나 행렬 계산할 때 사용됨
# norm = 벡터의 길이
def cosine_similarity(emb1, emb2):
    return np.dot(emb1, emb2) / (np.linalg.norm(emb1) * np.linalg.norm(emb2))

# 반환: 유사도(T/F), score 값
def face_check(db_face_img, curr_face_img):
    # CPU 모드
    model = FaceAnalysis(name="buffalo_l")
    model.prepare(ctx_id=-1, det_size=(640, 640))

    db_face = model.get(db_face_img)
    curr_face = model.get(curr_face_img)

    if len(db_face) == 0 or len(curr_face) == 0:
        return False

    db_emb = db_face[0].embedding
    curr_emb = curr_face[0].embedding

    score = cosine_similarity(db_emb, curr_emb)
    print(f'유사도: {score}')
    return score > 0.3, float(np.round(score, 2))
