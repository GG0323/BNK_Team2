from fastapi import APIRouter

router = APIRouter(
    prefix="/fast/api/check",
    tags=["Check_Router"]
)

@router.get("")
def router_check():
    return{
        "status" : "OK",
        "message" : "FastAPI AI Server is running"
    }