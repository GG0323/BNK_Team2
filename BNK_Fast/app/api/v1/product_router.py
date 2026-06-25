from typing import Any, Dict

from fastapi import APIRouter
from pydantic import BaseModel

from app.schemas.product_schema import (
    ProductCompareRequest,
    ProductRecommendRequest,
    ProductSummaryRequest,
)
from app.services.product_ai_service import ProductAiService

router = APIRouter(
    prefix="/fast/api/ai",
    tags=["product-ai"]
)

product_ai_service = ProductAiService()


@router.post("/2/product/summary")
def product_summary(payload: ProductSummaryRequest) -> Dict[str, Any]:
    product_dto = payload.product

    if product_dto is None:
        return {
            "answer": "요약할 상품 정보가 없습니다.",
            "status": "NOT_FOUND"
        }

    product = to_plain_dict(product_dto)
    product_name = product_ai_service.get_text(product, "product_name", "productName")

    if product_name.strip() == "":
        return {
            "answer": "상품명이 비어 있어 요약을 생성할 수 없습니다.",
            "status": "NOT_FOUND"
        }

    result = product_ai_service.create_product_summary(product)

    return {
        "answer": result["answer"],
        "status": result["status"],
        "productName": product_name,
        "source": result["source"]
    }


@router.post("/2/product/compare")
def product_compare(payload: ProductCompareRequest) -> Dict[str, Any]:
    product_dtos = payload.products or []
    products = [to_plain_dict(product_dto) for product_dto in product_dtos]

    if len(products) == 0:
        return {
            "answer": "비교할 상품 정보가 없습니다.",
            "status": "NOT_FOUND",
            "candidates": []
        }

    if len(products) == 1:
        result = product_ai_service.create_product_summary(products[0])

        return {
            "answer": result["answer"],
            "status": "SINGLE_FOUND",
            "candidates": products,
            "source": result["source"]
        }

    result = product_ai_service.create_compare_summary(products)

    return {
        "answer": result["answer"],
        "status": result["status"],
        "candidates": products,
        "source": result["source"]
    }


@router.post("/2/product/recommend")
def product_recommend(payload: ProductRecommendRequest) -> Dict[str, Any]:
    request = to_plain_dict(payload)
    products = request.get("products") or []

    if len(products) == 0:
        return {
            "summary": "추천 후보 상품 정보가 없습니다.",
            "status": "NOT_FOUND",
            "recommendedProducts": [],
            "source": "fallback"
        }

    result = product_ai_service.create_persona_recommend(request)

    return {
        "summary": result["summary"],
        "status": result["status"],
        "recommendedProducts": result["recommendedProducts"],
        "source": result["source"]
    }


def to_plain_dict(model: BaseModel) -> Dict[str, Any]:
    """
    Pydantic v1/v2 모두 대응하기 위한 dict 변환 함수.
    빈 문자열/None은 제거하고, nested model도 dict로 변환한다.
    """

    if hasattr(model, "model_dump"):
        data = model.model_dump()
    else:
        data = model.dict()

    cleaned = {}

    for key, value in data.items():
        if value is None:
            continue

        if isinstance(value, str) and value.strip() == "":
            continue

        if isinstance(value, list):
            cleaned_list = []

            for item in value:
                if isinstance(item, BaseModel):
                    cleaned_list.append(to_plain_dict(item))
                elif isinstance(item, dict):
                    cleaned_list.append(item)
                else:
                    cleaned_list.append(item)

            cleaned[key] = cleaned_list
            continue

        cleaned[key] = value

    return cleaned