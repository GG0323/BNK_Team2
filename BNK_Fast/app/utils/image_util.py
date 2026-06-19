import numpy as np
import cv2

# 유틸: 암호화된 이미지 bytes → OpenCV 이미지
def bytes_to_cv2(image_bytes: bytes):
    np_arr = np.frombuffer(image_bytes, dtype=np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    return img


# 유틸: OpenCV 이미지 → bytes
def cv2_to_bytes(img) -> bytes:
    success, encoded = cv2.imencode(".jpg", img)
    if not success:
        raise ValueError("이미지 인코딩 실패")
    return encoded.tobytes()