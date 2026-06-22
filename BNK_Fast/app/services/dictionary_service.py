from typing import Any, Dict, List
from app.repositories import dictionary_repository
from app.services.openai_dictionary_llm_service import OpenAiDictionaryLlmService

class DictionaryService:
    def __init__(self):
        self.llm_service = OpenAiDictionaryLlmService()

    def search_dictionaries_for_chat(self, question: str) -> List[Dict[str, Any]]:
        keyword = self.extract_keyword(question)

        # "뭐야?", "알려줘"처럼 키워드가 비어버리는 질문은 DB 전체 검색을 막음
        if keyword.strip() == "":
            return []

        results = dictionary_repository.search_dictionary_for_chat(question, keyword)

        if not results:
            return []

        valid_results = []

        for dto in results:
            if self.is_valid_term_match(question, keyword, dto.get("dictionary_nm")):
                valid_results.append(dto)

        return valid_results

    def search_category_dictionaries_for_chat(self, question: str) -> List[Dict[str, Any]]:
        if question is None or question.strip() == "":
            return []

        category = self.extract_category_from_question(question)

        if category is None:
            return []

        return dictionary_repository.select_dictionary_by_category(category)

    def increase_view_count(self, dictionary_no: int) -> None:
        dictionary_repository.update_view_count(dictionary_no)

    def extract_keyword(self, question: str) -> str:
        if question is None:
            return ""

        return (
            question
            .replace("이 뭐야", "")
            .replace("가 뭐야", "")
            .replace("은 뭐야", "")
            .replace("는 뭐야", "")
            .replace("뭐야", "")
            .replace("알려줘", "")
            .replace("설명해줘", "")
            .replace("?", "")
            .strip()
        )

    def is_valid_term_match(self, question: str, keyword: str, term: str | None) -> bool:
        if question is None or term is None or term.strip() == "":
            return False

        cleaned_keyword = "" if keyword is None else keyword.strip()
        cleaned_term = term.strip()

        # 사용자가 정확히 용어명을 입력한 경우
        if cleaned_keyword == cleaned_term:
            return True

        # 질문 문장 안에서 용어가 독립적으로 등장하는 경우
        return self.contains_as_term(question, cleaned_term)

    def contains_as_term(self, question: str, term: str) -> bool:
        index = question.find(term)

        while index >= 0:
            start = index
            end = index + len(term)

            left_ok = start == 0 or not self.is_korean_or_alpha_numeric(question[start - 1])

            right_ok = (
                end >= len(question)
                or not self.is_korean_or_alpha_numeric(question[end])
                or self.is_allowed_josa(question[end])
            )

            if left_ok and right_ok:
                return True

            index = question.find(term, index + 1)

        return False

    def is_korean_or_alpha_numeric(self, ch: str) -> bool:
        if not ch:
            return False

        return (
            ("가" <= ch <= "힣")
            or ("A" <= ch <= "Z")
            or ("a" <= ch <= "z")
            or ("0" <= ch <= "9")
        )

    def is_allowed_josa(self, ch: str) -> bool:
        return ch in ["이", "가", "은", "는", "을", "를", "와", "과", "도", "만", "에", "의", "로", "랑"]

    def is_category_list_question(self, question: str) -> bool:
        if question is None:
            return False

        return (
            "종류" in question
            or "목록" in question
            or "관련" in question
            or "뭐 있어" in question
            or "어떤" in question
            or "리스트" in question
        )

    def extract_category_from_question(self, question: str) -> str | None:
        if "금리" in question:
            return "금리"

        if "해지" in question or "만기" in question:
            return "만기/해지"

        if "가입" in question:
            return "가입조건"

        if "납입" in question:
            return "납입"

        if "세금" in question or "보호" in question or "비과세" in question:
            return "세금/보호"

        if "상품" in question or "통장" in question:
            return "상품유형"

        if "예금" in question or "적금" in question or "예적금" in question:
            return "예적금"

        return None

    def is_compare_question(self, question: str) -> bool:
        if question is None:
            return False

        return (
            "차이" in question
            or "비교" in question
            or "다른" in question
            or "달라" in question
        )

    def make_list_answer(self, results: List[Dict[str, Any]]) -> str:
        answer = "질문과 관련된 금융용어 목록입니다.\n\n"

        for index, dto in enumerate(results, start=1):
            answer += f"{index}. {dto.get('dictionary_nm')} - {dto.get('dictionary_category')}\n"

        answer += "\n더 자세히 알고 싶은 용어를 입력해 주세요."

        return answer

    def make_compare_fallback_answer(self, results: List[Dict[str, Any]]) -> str:
        answer = "질문하신 용어들의 차이는 다음과 같습니다.\n\n"

        for dto in results:
            answer += f"[{dto.get('dictionary_nm')}]\n"
            answer += f"{dto.get('dictionary_content')}\n\n"

        return answer

    def generate_llm_answer_for_single(self, question: str, dto: Dict[str, Any]) -> str:
        prompt = f"""
                    [사용자 질문]
                    {question}

                    [DB 검색 결과]
                    용어명: {dto.get("dictionary_nm")}
                    카테고리: {dto.get("dictionary_category")}
                    설명: {dto.get("dictionary_content")}

                    [답변 지시]
                    위 DB 검색 결과만 사용해서 답변해라.
                    DB에 없는 추가 금융정보는 말하지 마라.
                    답변은 한국어로 자연스럽고 문법에 맞게 작성해라.
                    사용자가 이해하기 쉽게 2문장으로 설명해라.
                    첫 문장은 용어의 뜻을 설명해라.
                    두 번째 문장은 조건, 주의점, 특징 중 DB 설명에 있는 내용을 바탕으로 설명해라.
                    말투는 친절한 설명체로 작성해라.
                """.strip()

        return self.llm_service.generate_dictionary_answer(prompt)

    def generate_llm_answer_for_compare(self, question: str, results: List[Dict[str, Any]]) -> str:
        db_text = ""

        for dto in results:
            db_text += f"용어명: {dto.get('dictionary_nm')}\n"
            db_text += f"카테고리: {dto.get('dictionary_category')}\n"
            db_text += f"설명: {dto.get('dictionary_content')}\n\n"

        prompt = f"""
[사용자 질문]
{question}

[DB 검색 결과]
{db_text}

[답변 지시]
위 DB 검색 결과만 사용해서 각 용어의 차이를 설명해라.
먼저 한 문장으로 핵심 차이를 말하고,
그 다음 용어별 설명을 짧게 정리해라.
DB에 없는 내용은 추측하지 마라.
""".strip()

        return self.llm_service.generate_dictionary_answer(prompt)
