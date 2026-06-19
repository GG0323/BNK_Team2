from app.core.database import get_connection


# 신분증 이미지 조회 (Flutter가 암호화 후 저장한 것)
def get_idcard_image(member_no: int):
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            """
            SELECT idcard_image_enc, idcard_image_nonce, idcard_image_tag
            FROM tb_authentication
            WHERE member_no = :member_no
            """,
            {"member_no": member_no}
        )

        row = cursor.fetchone()
        if row is None:
            return None

        return {
            "idcard_image_enc":   row[0].read() if hasattr(row[0], 'read') else row[0],
            "idcard_image_nonce": row[1],
            "idcard_image_tag":   row[2]
        }
    finally:
        cursor.close()
        conn.close()


# 신분증 내 얼굴 + OCR 결과 저장
def save_idcard_result(
    member_no: int,
    idface_enc: str,
    idface_nonce: str,
    idface_tag: str,
    ocr_enc: str,
    ocr_nonce: str,
    ocr_tag: str
):
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            """
            UPDATE tb_authentication
            SET idface_image_enc   = :idface_enc,
                idface_image_nonce = :idface_nonce,
                idface_image_tag   = :idface_tag,
                ocr_result_enc     = :ocr_enc,
                ocr_result_nonce   = :ocr_nonce,
                ocr_result_tag     = :ocr_tag
            WHERE member_no = :member_no
            """,
            {
                "idface_enc":   idface_enc,
                "idface_nonce": idface_nonce,
                "idface_tag":   idface_tag,
                "ocr_enc":      ocr_enc,
                "ocr_nonce":    ocr_nonce,
                "ocr_tag":      ocr_tag,
                "member_no":    member_no
            }
        )
        conn.commit()
        return cursor.rowcount > 0
    except Exception as e:
        conn.rollback()
        print(f"[save_idcard_result] DB 저장 실패: {e}")
        return False
    finally:
        cursor.close()
        conn.close()


# 얼굴 이미지 조회 (현재 얼굴 사진 촬영한 이미지 + 신분증 얼굴 이미지)
def get_face_image(member_no: int):
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            """
            SELECT face_image_enc, face_image_nonce, face_image_tag,
                   idface_image_enc, idface_image_nonce, idface_image_tag
            FROM tb_authentication
            WHERE member_no = :member_no
            """,
            {"member_no": member_no}
        )

        row = cursor.fetchone()
        if row is None:
            return None

        return {
            "face_image_enc":     row[0].read() if hasattr(row[0], 'read') else row[0],
            "face_image_nonce":   row[1],
            "face_image_tag":     row[2],
            "idface_image_enc":   row[3].read() if hasattr(row[3], 'read') else row[3],
            "idface_image_nonce": row[4],
            "idface_image_tag":   row[5]
        }
    finally:
        cursor.close()
        conn.close()


# 유사도 점수 저장
def save_similarity_score(member_no: int, score: float):
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            """
            UPDATE tb_authentication
            SET similarity_score = :score
            WHERE member_no = :member_no
            """,
            {
                "score":     round(score, 2),
                "member_no": member_no
            }
        )
        conn.commit()
        return cursor.rowcount > 0
    except Exception as e:
        conn.rollback()
        print(f"[save_similarity_score] DB 저장 실패: {e}")
        return False
    finally:
        cursor.close()
        conn.close()