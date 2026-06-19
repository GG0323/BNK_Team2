from pydantic import BaseModel

# 신분증에서 추출한 텍스트를 클라이언트로 반환하는 DTO
# 준회원 이상 정회원 미만인 경우 사용할 예정이라 회원 Pk가 존재함
class OcrResultResponse(BaseModel):
    pk : int
    result : bool

# 얼굴 유사도 결과 반환
class FaceResultResponse(BaseModel):
    pk: int
    result: bool