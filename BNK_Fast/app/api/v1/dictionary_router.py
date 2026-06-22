from typing import Any, Dict

from fastapi import APIRouter
from pydantic import BaseModel

from app.services.dictionary_service import DictionaryService

router = APIRouter(
    prefix="/fast/api/ai",
    tags=["dictionary-ai"]
)

dictionary_service = DictionaryService()


class DictionaryRequest(BaseModel):
    query: str | None = None
    history: list[dict[str, str]] | None = None


@router.post("/2/dictionary")
def dictionary(payload: DictionaryRequest) -> Dict[str, Any]:
    question = payload.query

    if question is None or question.strip() == "":
        return {
            "answer": "질문을 입력해 주세요.",
            "status": "NOT_FOUND"
        }

    question = question.strip()

    if dictionary_service.is_category_list_question(question):
        category_results = dictionary_service.search_category_dictionaries_for_chat(question)

        if category_results:
            return {
                "answer": dictionary_service.make_list_answer(category_results),
                "status": "MULTI_FOUND",
                "candidates": category_results
            }

    results = dictionary_service.search_dictionaries_for_chat(question)

    if not results:
        return {
            "answer": "등록된 금융용어사전에서 해당 내용을 찾을 수 없습니다.",
            "status": "NOT_FOUND"
        }

    if len(results) >= 2 and dictionary_service.is_compare_question(question):
        for item in results:
            dictionary_service.increase_view_count_safely(item.get("dictionary_no"))

        answer = dictionary_service.make_compare_fallback_answer(results)

        try:
            llm_answer = dictionary_service.generate_llm_answer_for_compare(question, results)
            if llm_answer is not None and llm_answer.strip() != "":
                answer = llm_answer
        except Exception as e:
            print(f"LLM 비교 답변 생성 실패. 기본 답변 사용. error={e}")

        return {
            "answer": answer,
            "status": "COMPARE",
            "candidates": results
        }

    if len(results) >= 2:
        return {
            "answer": dictionary_service.make_list_answer(results),
            "status": "MULTI_FOUND",
            "candidates": results
        }

    dto = results[0]
    dictionary_service.increase_view_count_safely(dto.get("dictionary_no"))

    answer = f"{dto['dictionary_nm']}은(는) {dto['dictionary_content']}"

    try:
        llm_answer = dictionary_service.generate_llm_answer_for_single(question, dto)
        if llm_answer is not None and llm_answer.strip() != "":
            answer = llm_answer
    except Exception as e:
        print(f"LLM 단일 답변 생성 실패. 기본 답변 사용. error={e}")

    return {
        "answer": answer,
        "status": "FOUND",
        "dictionaryNo": dto["dictionary_no"],
        "dictionaryName": dto["dictionary_nm"],
        "category": dto["dictionary_category"],
        "source": dto.get("source", "db")
    }
