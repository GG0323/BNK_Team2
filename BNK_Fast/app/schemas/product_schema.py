from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class ProductDto(BaseModel):
    """
    Spring 상품 DTO를 FastAPI에서 받기 위한 모델.
    Spring 쪽에서 snake_case로 와도 받고, camelCase 값이 extra로 들어와도 서비스에서 읽을 수 있게 둔다.
    """

    product_no: Optional[int] = 0
    product_name: Optional[str] = ""
    product_type: Optional[str] = ""

    subtitle: Optional[str] = ""
    content: Optional[str] = ""

    min_interest_rate: Optional[float] = 0
    max_interest_rate: Optional[float] = 0

    interest_payment_type: Optional[str] = ""
    interest_calc_type: Optional[str] = ""

    branch_join_yn: Optional[str] = "N"
    internet_join_yn: Optional[str] = "N"
    mobile_join_yn: Optional[str] = "N"

    join_method_desc: Optional[str] = ""

    min_join_amount: Optional[int] = 0
    max_join_amount: Optional[int] = 0
    deposit_unit: Optional[int] = 0

    min_term_months: Optional[int] = 0
    max_term_months: Optional[int] = 0
    fixed_term_yn: Optional[str] = "N"
    fixed_term_values: Optional[str] = ""

    condition_note: Optional[str] = ""
    depositor_protection_yn: Optional[str] = "Y"

    maturity_rate_label: Optional[str] = ""
    maturity_annual_rate: Optional[str] = ""
    maturity_return_rate: Optional[str] = ""

    after_maturity_rate_label: Optional[str] = ""
    after_maturity_annual_rate: Optional[str] = ""

    early_rate_label: Optional[str] = ""
    early_annual_rate: Optional[str] = ""

    preferential_rate_summary: Optional[str] = ""

    additionalProp1: Dict[str, Any] = Field(default_factory=dict)

    class Config:
        extra = "allow"


class ProductSummaryRequest(BaseModel):
    product: ProductDto

    class Config:
        extra = "allow"


class ProductCompareRequest(BaseModel):
    products: List[ProductDto] = Field(default_factory=list)

    class Config:
        extra = "allow"


class ProductRecommendRequest(BaseModel):
    age: Optional[int] = 0
    balance: Optional[int] = 0
    monthlyAmount: Optional[int] = 0
    periodMonths: Optional[int] = 0

    purpose: Optional[str] = "MAKE_MONEY"
    preferredProductType: Optional[str] = "ALL"
    preferredChannel: Optional[str] = "ALL"
    interestConditions: List[str] = Field(default_factory=list)

    products: List[ProductDto] = Field(default_factory=list)

    additionalProp1: Dict[str, Any] = Field(default_factory=dict)

    class Config:
        extra = "allow"