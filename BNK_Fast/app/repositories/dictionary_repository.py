import oracledb
from datetime import date, datetime
from decimal import Decimal
from typing import Any, Dict, List
from app.core.database import get_connection

# 금융용어사전 조회
def search_dictionary_for_chat(question: str, keyword: str) -> List[Dict[str, Any]]:
    sql = """
        SELECT *
        FROM (
            SELECT
                dictionary_no,
                dictionary_nm,
                dictionary_content,
                dictionary_category,
                view_count,
                created_at,
                updated_at
            FROM tb_finance_dictionary
            WHERE INSTR(:question, dictionary_nm) > 0
                OR dictionary_nm LIKE '%' || :keyword || '%'
                OR DBMS_LOB.INSTR(dictionary_content, :keyword) > 0
            ORDER BY
                CASE
                    WHEN dictionary_nm = :keyword THEN 1
                    WHEN INSTR(:question, dictionary_nm) > 0 THEN 2
                    WHEN dictionary_nm LIKE '%' || :keyword || '%' THEN 3
                    WHEN DBMS_LOB.INSTR(dictionary_content, :keyword) > 0 THEN 4
                    ELSE 5
                END,
                view_count DESC
        )
        WHERE ROWNUM <= 5
    """

    return fetch_all(sql, {
        "question": question,
        "keyword": keyword
    })

# 금융용어사전 카테고리 조회
def select_dictionary_by_category(category: str) -> List[Dict[str, Any]]:
    sql = """
        SELECT
            dictionary_no,
            dictionary_nm,
            dictionary_content,
            dictionary_category,
            view_count,
            created_at,
            updated_at
        FROM tb_finance_dictionary
        WHERE dictionary_category = :category
            AND dictionary_nm != :category
        ORDER BY dictionary_no ASC
    """

    return fetch_all(sql, {
        "category": category
    })

# 금융용어사전 조회수 업데이트
def update_view_count(dictionary_no: int):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                    UPDATE tb_finance_dictionary
                    SET view_count = view_count + 1
                    WHERE dictionary_no = :dictionary_no
                """,
                {
                    "dictionary_no": dictionary_no
                }
            )

        connection.commit()

    finally:
        connection.close()

# SQL문 실행문
def fetch_all(sql: str, params: Dict[str, Any]) -> List[Dict[str, Any]]:
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute(sql, params)

            columns = [col[0].lower() for col in cursor.description]
            rows = cursor.fetchall()

            result = []

            for row in rows:
                item = {}

                for index, value in enumerate(row):
                    item[columns[index]] = to_json_safe_value(value)

                result.append(item)

            return result

    finally:
        connection.close()

# json으로 변환
def to_json_safe_value(value: Any) -> Any:
    if isinstance(value, oracledb.LOB):
        return value.read()

    if isinstance(value, (datetime, date)):
        return value.isoformat()

    if isinstance(value, Decimal):
        if value == value.to_integral_value():
            return int(value)
        return float(value)

    return value