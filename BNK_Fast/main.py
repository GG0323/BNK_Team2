from fastapi import FastAPI
from app.api.v1.auth_router import router as auth_router
from app.api.v1.check_router import router as check_router
from app.api.v1.dictionary_router import router as dictionary_router
from app.api.v1.faq_router import router as faq_router
from app.api.v1.product_router import router as product_router

app = FastAPI(
    title="BNK AI Inference Server",
    description="Spring 서버와 연동되는 AI 서버",
    version="1.0.0"
)

# 라우터 등록
app.include_router(check_router)
app.include_router(auth_router)
app.include_router(dictionary_router)
app.include_router(faq_router)
app.include_router(product_router)
 
 
@app.get("/")
def index():
    return {
        "server": "BNK AI Inference Server",
        "status": "running"
    }
 