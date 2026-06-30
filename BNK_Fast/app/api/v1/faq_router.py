from typing import Any, Dict, List, Literal

from fastapi import APIRouter
from pydantic import BaseModel

from app.services.faq_admin_service import FaqAdminService
from app.services.faq_llm_service import FaqLlmService
from app.services.faq_pipeline_service import FaqPipelineService

router = APIRouter(
    prefix="/fast/api/ai",
    tags=["faq-ai"]
)

faq_service = FaqLlmService()
faq_admin_service = FaqAdminService()
#추가
faq_pipeline_service = FaqPipelineService()

#추가
class ChatMessage(BaseModel):
    role: Literal["user", "assistant"]
    content: str

class FaqRequest(BaseModel):
    query: str | None = None
    #추가 이전 대화 (없으면 빈 리스트 -> 단발 처리)
    history: List[ChatMessage] = []

class FaqAddRequest(BaseModel):
    question: str
    answer: str


 
# FaQ 컨트롤러  
@router.post("/2/faq")
def faq(payload: FaqRequest) -> Dict[str, Any]:
    question = payload.query

    if question is None or question.strip() == "":
        return {
            "answer": "질문을 입력해 주세요.",
            "status": "NOT_FOUND"
        }

    #추가
    history = [msg.model_dump() for msg in payload.history]
    return faq_service.search_faq(question.strip(), history=history)

# faqs.json 최신화, 백터 DB 리빌드  /fast/api/ai/2/faqs
@router.post("/2/faqs", response_model=bool)
async def add_faq(payload: FaqAddRequest) -> bool:
    print(await FaqAddRequest.body())
    return faq_admin_service.add_faq(payload.question, payload.answer)

# 파이프라인 호출 컨트롤러  /fast/api/ai/2/faq/refresh
@router.post("/2/faq/refresh")
def refresh_candidates() -> Dict[str, Any]:
    return faq_pipeline_service.refresh_candidates()