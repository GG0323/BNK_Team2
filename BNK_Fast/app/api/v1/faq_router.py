from typing import Any, Dict

from fastapi import APIRouter
from pydantic import BaseModel

from app.services.faq_admin_service import FaqAdminService
from app.services.faq_llm_service import FaqLlmService

router = APIRouter(
    prefix="/fast/api/ai",
    tags=["faq-ai"]
)

faq_service = FaqLlmService()
faq_admin_service = FaqAdminService()


class FaqRequest(BaseModel):
    query: str | None = None


class FaqAddRequest(BaseModel):
    question: str
    answer: str


@router.post("/2/faq")
def faq(payload: FaqRequest) -> Dict[str, Any]:
    question = payload.query

    if question is None or question.strip() == "":
        return {
            "answer": "질문을 입력해 주세요.",
            "status": "NOT_FOUND"
        }

    return faq_service.search_faq(question.strip())


@router.post("/2/faqs", response_model=bool)
def add_faq(payload: FaqAddRequest) -> bool:
    return faq_admin_service.add_faq(payload.question, payload.answer)
