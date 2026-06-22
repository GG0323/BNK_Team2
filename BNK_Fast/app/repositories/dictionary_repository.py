from datetime import date, datetime
from decimal import Decimal
from typing import Any, Dict, List

import oracledb

from app.core.database import get_connection


class DictionaryRepository:
    def search_dictionary_for_chat(
        self,
        question: str,
        keyword: str,
        normalized_question: str,
        normalized_keyword: str
    ) -> List[Dict[str, Any]]:
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
                WHERE
                    INSTR(:question, dictionary_nm) > 0
                    OR dictionary_nm LIKE '%' || :keyword || '%'
                    OR DBMS_LOB.INSTR(dictionary_content, :keyword) > 0
                    OR INSTR(:normalized_question, REPLACE(dictionary_nm, ' ', '')) > 0
                    OR REPLACE(dictionary_nm, ' ', '') LIKE '%' || :normalized_keyword || '%'
                    OR DBMS_LOB.INSTR(
                        REPLACE(dictionary_content, ' ', ''),
                        :normalized_keyword
                    ) > 0
                ORDER BY
                    CASE
                        WHEN dictionary_nm = :keyword THEN 1
                        WHEN REPLACE(dictionary_nm, ' ', '') = :normalized_keyword THEN 2
                        WHEN INSTR(:question, dictionary_nm) > 0 THEN 3
                        WHEN INSTR(:normalized_question, REPLACE(dictionary_nm, ' ', '')) > 0 THEN 4
                        WHEN dictionary_nm LIKE '%' || :keyword || '%' THEN 5
                        WHEN REPLACE(dictionary_nm, ' ', '') LIKE '%' || :normalized_keyword || '%' THEN 6
                        WHEN DBMS_LOB.INSTR(dictionary_content, :keyword) > 0 THEN 7
                        ELSE 8
                    END,
                    view_count DESC
            )
            WHERE ROWNUM <= 5
        """

        return self._fetch_all(sql, {
            "question": question,
            "keyword": keyword,
            "normalized_question": normalized_question,
            "normalized_keyword": normalized_keyword
        })

    def select_dictionary_by_category(self, category: str) -> List[Dict[str, Any]]:
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

        return self._fetch_all(sql, {
            "category": category
        })

    def update_view_count(self, dictionary_no: int) -> None:
        connection = get_connection()

        try:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    UPDATE tb_finance_dictionary
                    SET view_count = view_count + 1
                    WHERE dictionary_no = :dictionary_no
                    """,
                    {"dictionary_no": dictionary_no}
                )

            connection.commit()

        finally:
            connection.close()

    def _fetch_all(self, sql: str, params: Dict[str, Any]) -> List[Dict[str, Any]]:
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
                        item[columns[index]] = self._to_json_safe_value(value)
                    result.append(item)

                return result

        finally:
            connection.close()

    def _to_json_safe_value(self, value: Any) -> Any:
        if isinstance(value, oracledb.LOB):
            return value.read()

        if isinstance(value, (datetime, date)):
            return value.isoformat()

        if isinstance(value, Decimal):
            if value == value.to_integral_value():
                return int(value)
            return float(value)

        return value
