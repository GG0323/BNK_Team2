import re
from typing import Any, Dict, List

import chromadb
from openai import OpenAI

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL
from app.repositories.dictionary_repository import DictionaryRepository
from app.services.openai_dictionary_llm_service import OpenAiDictionaryLlmService

DICTIONARY_CHROMA_PATH = str(BASE_DIR / "vector_stores" / "chroma_dictionary_db")
DICTIONARY_COLLECTION_NAME = "finance_dictionary"
DICTIONARY_VECTOR_MAX_DISTANCE = 1.2


class DictionaryService:
    def __init__(self):
        self.repository = DictionaryRepository()
        self.llm_service = OpenAiDictionaryLlmService()
        self.embedding_client = None
        self.vector_collection = None

        try:
            chroma_client = chromadb.PersistentClient(path=DICTIONARY_CHROMA_PATH)
            self.vector_collection = chroma_client.get_collection(name=DICTIONARY_COLLECTION_NAME)
        except Exception as e:
            print(
                "금융용어 벡터 DB 컬렉션을 찾을 수 없습니다. "
                "먼저 scripts/build_dictionary_db.py를 실행하세요. "
                f"path={DICTIONARY_CHROMA_PATH}, error={e}"
            )

    def search_dictionaries_for_chat(self, question: str) -> List[Dict[str, Any]]:
        keyword = self.extract_keyword(question)

        if keyword.strip() == "":
            return []

        normalized_question = self.normalize_text(question)
        normalized_keyword = self.normalize_text(keyword)

        db_results = self.repository.search_dictionary_for_chat(
            question=question,
            keyword=keyword,
            normalized_question=normalized_question,
            normalized_keyword=normalized_keyword
        )

        valid_results = []
        for dto in db_results:
            if self.is_valid_term_match(question, keyword, dto.get("dictionary_nm")):
                dto["source"] = "db"
                valid_results.append(dto)

        if valid_results:
            return valid_results

        vector_results = self.search_vector_dictionaries(question, n_results=5)
        for dto in vector_results:
            dto["source"] = "vector"

        return vector_results

    def search_vector_dictionaries(self, query: str, n_results: int = 5) -> List[Dict[str, Any]]:
        if self.vector_collection is None:
            return []

        if query is None or query.strip() == "":
            return []

        response = self.get_embedding_client().embeddings.create(
            model=OPENAI_EMBED_MODEL or "text-embedding-3-small",
            input=query
        )
        vector = response.data[0].embedding

        results = self.vector_collection.query(
            query_embeddings=[vector],
            n_results=n_results,
            include=["documents", "metadatas", "distances"]
        )

        output = []
        ids = results.get("ids", [[]])[0]
        documents = results.get("documents", [[]])[0]
        metadatas = results.get("metadatas", [[]])[0]
        distances = results.get("distances", [[]])[0]

        for i in range(len(ids)):
            distance = distances[i] if i < len(distances) else None

            if distance is not None and distance > DICTIONARY_VECTOR_MAX_DISTANCE:
                continue

            metadata = metadatas[i] if i < len(metadatas) else {}
            document = documents[i] if i < len(documents) else ""

            output.append({
                "dictionary_no": metadata.get("dictionary_no"),
                "dictionary_nm": metadata.get("dictionary_nm"),
                "dictionary_content": metadata.get("dictionary_content", document),
                "dictionary_category": metadata.get("dictionary_category"),
                "vector_distance": distance
            })

        return output

    def get_embedding_client(self) -> OpenAI:
        if OPENAI_API_KEY is None or OPENAI_API_KEY.strip() == "":
            raise RuntimeError("OPENAI_API_KEY 환경변수가 설정되어 있지 않습니다.")

        if self.embedding_client is None:
            self.embedding_client = OpenAI(api_key=OPENAI_API_KEY)

        return self.embedding_client

    def search_category_dictionaries_for_chat(self, question: str) -> List[Dict[str, Any]]:
        if question is None or question.strip() == "":
            return []

        category = self.extract_category_from_question(question)

        if category is None:
            return []

        return self.repository.select_dictionary_by_category(category)

    def increase_view_count_safely(self, dictionary_no: Any) -> None:
        if dictionary_no is None:
            return

        try:
            self.repository.update_view_count(int(dictionary_no))
        except Exception as e:
            print(f"조회수 증가 실패. dictionary_no={dictionary_no}, error={e}")

    def extract_keyword(self, question: str) -> str:
        if question is None:
            return ""

        keyword = question.strip()
        remove_words = [
            "이게 뭐야",
            "가 뭐야",
            "은 뭐야",
            "는 뭐야",
            "뭐야",
            "무엇이야",
            "무슨 뜻이야",
            "뜻이 뭐야",
            "알려줘",
            "설명해줘",
            "설명해",
            "궁금해",
            "?"
        ]

        for word in remove_words:
            keyword = keyword.replace(word, "")

        return keyword.strip()

    def normalize_text(self, text: str | None) -> str:
        if text is None:
            return ""

        text = text.lower()
        text = re.sub(r"\s+", "", text)
        text = re.sub(r"""[-_.,!?()\[\]{}<>"'`~:;·]""", "", text)

        return text.strip()

    def is_valid_term_match(self, question: str, keyword: str, term: str | None) -> bool:
        if question is None or term is None or term.strip() == "":
            return False

        cleaned_keyword = "" if keyword is None else keyword.strip()
        cleaned_term = term.strip()

        if cleaned_keyword == cleaned_term:
            return True

        if self.normalize_text(cleaned_keyword) == self.normalize_text(cleaned_term):
            return True

        if self.normalize_text(cleaned_term) in self.normalize_text(question):
            return True

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
        return ch in ["은", "는", "이", "가", "을", "를", "과", "와", "도", "만", "에", "로", "요"]

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

        if "입금" in question or "보호" in question or "비과세" in question:
            return "입금/보호"

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

[검색 결과]
용어명: {dto.get("dictionary_nm")}
카테고리: {dto.get("dictionary_category")}
설명: {dto.get("dictionary_content")}

[답변 지침]
- 검색 결과만 사용해서 답변해라.
- 검색 결과에 없는 추가 금융 정보는 말하지 마라.
- 한국어로 자연스럽고 문법에 맞게 작성해라.
- 사용자가 이해하기 쉽게 2문장으로 설명해라.
- 첫 문장은 용어의 뜻을 설명해라.
- 두 번째 문장은 조건, 주의점, 특징 중 검색 결과에 있는 내용만 바탕으로 설명해라.
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

[검색 결과]
{db_text}

[답변 지침]
- 검색 결과만 사용해서 각 용어의 차이를 설명해라.
- 먼저 한 문장으로 핵심 차이를 말하고, 그 다음 용어별 설명을 짧게 정리해라.
- 검색 결과에 없는 내용은 추측하지 마라.
""".strip()

        return self.llm_service.generate_dictionary_answer(prompt)
