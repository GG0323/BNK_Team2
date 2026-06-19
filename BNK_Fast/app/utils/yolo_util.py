from ultralytics import YOLO
import numpy as np
import cv2, uuid, time, json, requests, re
from app.core.config import SECRET_KEY, API_URL

def OCR_crop_merge(parts):
    # 여백 설정
    margin_x, margin_y, margin_h = 30, 15, 30

    # 원본 crop 중 가장 넓은 너비
    max_width = max(p.shape[1] for p in parts)

    # 최종 캔버스 폭 = 가장 넓은 crop + 좌우 여백
    canvas_width = max_width + (margin_x * 2)

    merged_parts = []

    for p in parts:
        h, w = p.shape[:2]

        # 각 crop을 담을 흰 배경 생성
        canvas_height = h + (margin_y * 2)
        canvas = 255 * np.ones((canvas_height, canvas_width, 3), dtype=p.dtype)

        # 가운데 정렬 좌표
        x_offset = (canvas_width - w) // 2
        y_offset = margin_y

        # 원본 crop을 흰 배경 가운데에 삽입
        canvas[
            y_offset:y_offset + h,
            x_offset:x_offset + w
        ] = p

        merged_parts.append(canvas)

        # 영역 구분용 흰 여백
        merged_parts.append(
            255 * np.ones((margin_h, canvas_width, 3), dtype=p.dtype)
        )

    return cv2.vconcat(merged_parts)


# 진짜 주민등록번호인지 확인 및 추출 잘못된 데이터 확인용
def check_sum(lst):
  if len(lst) != 13:
    return False
  
  weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5]
  ch_sum = sum(weights[i] * d for i, d in enumerate(list(map(int, [i for i in lst[:-1]]))))
  check = (11 - (ch_sum % 11)) % 10
  
  return check == int(lst[-1])


# OCR API 호출
def OCR_API_call(ocr_img):
    headers = {
        "X-OCR-SECRET": SECRET_KEY,
    }

    payload = {
        "version": "V2",
        "requestId": str(uuid.uuid4()),
        "timestamp": int(time.time() * 1000),
        "images": [
            {
                "format": "jpg",
                "name": "ocr"
            }
        ]
    }

    # OpenCV 이미지 객체를 jpg bytes로 변환
    success, encoded_img = cv2.imencode(".jpg", ocr_img)

    if not success:
        print("이미지 인코딩 실패")
        return False

    image_bytes = encoded_img.tobytes()

    files = {
        "message": (None, json.dumps(payload), "application/json"),
        "file": ("ocr.jpg", image_bytes, "image/jpeg")
    }

    response = requests.post(API_URL, headers=headers, files=files, timeout=10)

    print("status:", response.status_code)
    result_json = response.json()

    # 응답이 이상할 때
    if "images" not in result_json or len(result_json["images"]) <= 0:
        print("OCR 응답이 비정상..!!")
        print(result_json)
        return False

    return OCR_result(result_json)


def OCR_result(ocr_json):
    data = []

    for image in ocr_json["images"]:
        for field in image["fields"]:
            data.append(field["inferText"])

    # 닷(.) 없애기
    data = [i.replace(".", "") for i in data if i != "."]

    name = data[0]
    tmp = "".join(data[1:])

    # 숫자만 추출하기
    numbers = "".join(re.findall("[0-9]", tmp))

    # 주민등록번호 추출
    idnum = numbers[:13]

    # 주민등록번호 검증
    if not check_sum(idnum):
        print("잘못된 주민번호 입니다.")
        return False

    # 발급 날짜 추출하기
    date = numbers[13:]

    if len(date) == 8:
        ymd = [date[:4], date[4:6], date[6:]]
    else:
        ymd = [i for i in data if i in date]

    # 발급 기관 추출하기
    place = "".join(re.findall("[가-힣]", tmp))
    print(place)

    # 추출된 값 확인
    print(f"이름: {name}")
    print(f'주민번호: {idnum[:6]}-{idnum[6]}{"*" * (len(idnum[7:])-1)}')
    print(f'발급일자: {". ".join(ymd)}')
    print(f"발급기관: {place}")

    return {
        "name": name,
        "idnum": f'{idnum[:6]}-{idnum[6]}{"*" * (len(idnum[7:])-1)}',
        "date": f'{". ".join(ymd)}',
        "place": place
    }


# 객체탐지
def Object_detection(img):
    model = YOLO("save_models/bnk_best_ver6.pt")

    results = model(
        source=img,
        max_det=6
    )

    name, idnum, date, place = None, None, None, None
    idface_img = None

    for r in results:
        for b in r.boxes:
            x1, y1, x2, y2 = map(int, b.xyxy[0])
            crop = img[y1:y2, x1:x2]

            match int(b.cls.item()):
                case 1:
                    idface_img = crop
                case 2:
                    name = crop
                case 3:
                    idnum = crop
                case 4:
                    date = crop
                case 5:
                    place = crop

    parts = [name, idnum, date, place]
    label = ["이름", "주민번호", "날짜", "장소"]

    if any(p is None for p in parts):
        print('객체탐지 누락 발생!')
        print(f'객탐 실패한 객체: {[label[idx] for idx, data in enumerate(parts) if data is None]}')
        return False
    
    ocr_result = OCR_API_call(OCR_crop_merge(parts))

    return False if not ocr_result else {'ocr':ocr_result, 'idface':idface_img}
