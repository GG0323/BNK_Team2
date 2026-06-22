from typing import Any, Dict
from fastapi import APIRouter
from pydantic import BaseModel
from app.services.faq_llm_service import FaqLlmService

router = APIRouter(
    prefix="/fast/api/ai",
    tags=["faq-ai"]
)

faq_service = FaqLlmService()

class FaqRequest(BaseModel):
    query: str | None = None

@router.post("/2/faq")
def faq(payload: FaqRequest) -> Dict[str, Any]:
    question = payload.query

    if question is None or question.strip() == "":
        return {
            "answer": "질문을 입력해 주세요.",
            "status": "NOT_FOUND"
        }

    return faq_service.search_faq(question.strip())