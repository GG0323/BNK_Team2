from typing import List

from pydantic import BaseModel, Field


class ProductAiProductDto(BaseModel):
    """
    상품 AI 요약/비교/추천 요청에 사용하는 상품 DTO.
    Spring에서 FastAPI로 넘길 상품 정보 구조를 명시한다.
    """

    product_no: int | None = Field(default=None, description="상품 번호")
    product_name: str = Field(default="", description="상품명")
    product_type: str | None = Field(default="", description="상품 유형: DEPOSIT / SAVINGS")

    subtitle: str | None = Field(default="", description="상품 부제목")
    content: str | None = Field(default="", description="상품 설명")

    min_interest_rate: float | None = Field(default=0.0, description="최저 금리")
    max_interest_rate: float | None = Field(default=0.0, description="최고 금리")

    interest_payment_type: str | None = Field(default="", description="이자 지급 방식")
    interest_calc_type: str | None = Field(default="", description="이자 계산 방식")

    branch_join_yn: str | None = Field(default="N", description="영업점 가입 가능 여부")
    internet_join_yn: str | None = Field(default="N", description="인터넷 가입 가능 여부")
    mobile_join_yn: str | None = Field(default="N", description="모바일 가입 가능 여부")

    join_method_desc: str | None = Field(default="", description="가입 방법 설명")

    min_join_amount: int | None = Field(default=0, description="최소 가입 금액")
    max_join_amount: int | None = Field(default=0, description="최대 가입 금액")

    min_term_months: int | None = Field(default=0, description="최소 가입 기간")
    max_term_months: int | None = Field(default=0, description="최대 가입 기간")

    min_age: int | None = Field(default=0, description="최소 가입 나이")
    max_age: int | None = Field(default=0, description="최대 가입 나이")

    fixed_term_yn: str | None = Field(default="N", description="고정 기간 여부")
    fixed_term_values: str | None = Field(default="", description="고정 기간 값")

    condition_note: str | None = Field(default="", description="우대조건/가입조건")
    preferential_rate_summary: str | None = Field(default="", description="우대금리 요약")
    product_feature_desc: str | None = Field(default="", description="상품 특징")
    eligibility_desc: str | None = Field(default="", description="가입 대상 설명")
    period_desc: str | None = Field(default="", description="가입 기간 설명")
    amount_desc: str | None = Field(default="", description="가입 금액 설명")
    caution_note: str | None = Field(default="", description="유의사항")

    depositor_protection_yn: str | None = Field(default="Y", description="예금자보호 여부")

    maturity_rate_label: str | None = Field(default="", description="만기 금리 조건명")
    maturity_annual_rate: str | None = Field(default="", description="만기 연이율")
    maturity_return_rate: str | None = Field(default="", description="만기 연수익률")

    after_maturity_rate_label: str | None = Field(default="", description="만기 후 금리 조건명")
    after_maturity_annual_rate: str | None = Field(default="", description="만기 후 연이율")

    early_rate_label: str | None = Field(default="", description="중도해지 금리 조건명")
    early_annual_rate: str | None = Field(default="", description="중도해지 연이율")

    detail_url: str | None = Field(default="", description="상품 상세 URL")


class ProductSummaryRequest(BaseModel):
    product: ProductAiProductDto | None = Field(default=None, description="요약할 상품 정보")


class ProductCompareRequest(BaseModel):
    products: List[ProductAiProductDto] = Field(default_factory=list, description="비교할 상품 목록")


class ProductRecommendRequest(BaseModel):
    """
    AI 맞춤 상품 추천 요청 DTO.
    Spring의 ProductPersonaRecommendRequestDto 입력값과 추천 후보 상품 목록을 함께 받는다.
    """

    age: int = Field(default=0, description="고객 나이")
    balance: int = Field(default=0, description="현재 사용 가능 금액")
    monthlyAmount: int = Field(default=0, description="월 납입 가능 금액")
    periodMonths: int = Field(default=0, description="희망 가입 기간")

    purpose: str | None = Field(
        default="MAKE_MONEY",
        description="가입 목적: MAKE_MONEY / ROLL_MONEY / HIGH_RATE / EMERGENCY"
    )
    preferredProductType: str | None = Field(
        default="ALL",
        description="선호 상품 유형: ALL / DEPOSIT / SAVINGS"
    )
    preferredChannel: str | None = Field(
        default="ALL",
        description="선호 가입 채널: ALL / MOBILE / INTERNET / BRANCH"
    )

    interestConditions: List[str] = Field(default_factory=list, description="관심 조건 목록")
    products: List[ProductAiProductDto] = Field(default_factory=list, description="추천 후보 상품 목록")