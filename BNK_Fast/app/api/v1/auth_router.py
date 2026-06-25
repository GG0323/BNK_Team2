from fastapi import APIRouter, HTTPException
from app.schemas.response import OcrResultResponse, FaceResultResponse
from app.utils.crypto_util import decrypt_image, encrypt_image, encrypt_json
from app.utils.image_util import bytes_to_cv2, cv2_to_bytes
from app.utils.yolo_util import Object_detection
from app.utils.insightFace_util import face_check
from app.repositories.auth_repository import (
    get_idcard_image,
    get_face_image,
    save_idcard_result,
    save_similarity_score
)

router = APIRouter(
    prefix="/fast/api/auth",
    tags=["Object_Detection_Model_Router"]
)

# 신분증 검증
# Flutter → DB에 암호화된 신분증 이미지 저장 완료 후 호출
@router.post("/2/member", response_model=OcrResultResponse)
async def idcard_check(pk: int):
    # 1. 암호화된 신분증 이미지 조회
    idcard_data = get_idcard_image(pk)
    if idcard_data is None:
        raise HTTPException(status_code=404, detail="신분증 이미지를 찾을 수 없습니다.")
 
    # 2. 복호화 → OpenCV 이미지
    try:
        idcard_bytes = decrypt_image(
            idcard_data["idcard_image_enc"],
            idcard_data["idcard_image_nonce"],
            idcard_data["idcard_image_tag"]
        )
        idcard_img = bytes_to_cv2(idcard_bytes)
    except Exception as e:
        print(f"[idcard_check] 복호화 실패: {e}")
        raise HTTPException(status_code=500, detail="이미지 복호화에 실패했습니다.")
 
    # 3. YOLO 객체탐지 + OCR
    # Object_detection 반환값: { name, idnum, date, place } 또는 False
    detection_result = Object_detection(idcard_img)
 
    if detection_result is False:
        return OcrResultResponse(pk=pk, result=False)
 
    ocr_result = detection_result.get("ocr")       # OCR 텍스트 결과 dict
    idface_img = detection_result.get("idface")    # 신분증 내 얼굴 crop (numpy)
 
    if ocr_result is None or idface_img is None:
        return OcrResultResponse(pk=pk, result=False)
 
    # 4. 신분증 내 얼굴 + OCR 결과 암호화
    try:
        # OCR 결과 (dict → JSON → 암호화)
        ocr_encrypted = encrypt_json(ocr_result)

        # 신분증 내 얼굴 (numpy → bytes → 암호화)
        idface_encrypted = encrypt_image(cv2_to_bytes(idface_img))
        saved = save_idcard_result(
            member_no=pk,
            idface_enc=idface_encrypted["enc"],
            idface_nonce=idface_encrypted["nonce"],
            idface_tag=idface_encrypted["tag"],
            ocr_enc=ocr_encrypted["enc"],
            ocr_nonce=ocr_encrypted["nonce"],
            ocr_tag=ocr_encrypted["tag"]
        )

    except Exception as e:
        print(f"[idcard_check] 암호화 실패: {e}")
        raise HTTPException(status_code=500, detail="결과 암호화에 실패했습니다.")

    if not saved:
        raise HTTPException(status_code=500, detail="DB 저장에 실패했습니다.")
 
    return OcrResultResponse(pk=pk, result=True)
 

# 얼굴 검증 (InsightFace)
@router.post("/2/face", response_model=FaceResultResponse)
async def face_verify(pk: int):
    # 1. 두 이미지 조회
    face_data   = get_face_image(pk)
 
    if face_data is None:
        raise HTTPException(status_code=404, detail="실제 얼굴 이미지를 찾을 수 없습니다.")
 
    # 2. 복호화 → OpenCV 이미지
    try:
        face_bytes = decrypt_image(
            face_data["face_image_enc"],
            face_data["face_image_nonce"],
            face_data["face_image_tag"]
        )
        face_img = bytes_to_cv2(face_bytes)
 
        idface_bytes = decrypt_image(
            face_data["idface_image_enc"],
            face_data["idface_image_nonce"],
            face_data["idface_image_tag"]
        )
        idface_img = bytes_to_cv2(idface_bytes)
 
    except Exception as e:
        print(f"[face_verify] 복호화 실패: {e}")
        raise HTTPException(status_code=500, detail="이미지 복호화에 실패했습니다.")
 
    # 3. InsightFace 유사도 비교
    # face_check 반환: (bool, float) 또는 False
    result = face_check(idface_img, face_img)
    
    # 얼굴 탐지 자체 실패 (이미지에 얼굴 없음)
    if result is False:
        return FaceResultResponse(pk=pk, result=False)
 
    is_similar, score = result
 
    # 4. 유사도 점수 DB 저장
    save_similarity_score(member_no=pk, score=score)
 
    return FaceResultResponse(pk=pk, result=bool(is_similar))
