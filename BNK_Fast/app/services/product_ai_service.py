import json
import os
import urllib.error
import urllib.request
from typing import Any, Dict, List, Optional


class ProductAiService:
    """
    BNK 상품 AI 요약/비교/추천 서비스.

    포함 기능:
    1. 상품 1개 AI 요약
    2. 상품 여러 개 AI 비교 요약
    3. 고객 조건 기반 AI 맞춤 상품 추천
    """

    def __init__(self):
        self._load_secret_env()

        self.api_key = (
            os.getenv("OPENAI_API_KEY")
            or os.getenv("BNK_OPENAI_API_KEY")
            or os.getenv("SPRING_AI_OPENAI_API_KEY")
            or ""
        )

        self.model = (
            os.getenv("PRODUCT_AI_MODEL")
            or os.getenv("OPENAI_MODEL")
            or "gpt-5-nano"
        )

        self.timeout_seconds = int(os.getenv("PRODUCT_AI_TIMEOUT_SECONDS", "20"))

    # =========================================================
    # 상품 1개 AI 요약
    # =========================================================

    def create_product_summary(self, product: Dict[str, Any]) -> Dict[str, str]:
        if product is None:
            return {
                "answer": "요약할 상품 정보가 없습니다.",
                "status": "NOT_FOUND",
                "source": "fallback"
            }

        prompt = self._build_product_summary_prompt(product)
        llm_answer = self._generate_llm_answer(prompt)

        if llm_answer is not None and llm_answer.strip() != "":
            return {
                "answer": llm_answer.strip(),
                "status": "FOUND",
                "source": "llm"
            }

        return {
            "answer": self._create_fallback_product_summary(product),
            "status": "FOUND",
            "source": "fallback"
        }

    # =========================================================
    # 상품 여러 개 AI 비교 요약
    # =========================================================

    def create_compare_summary(self, products: List[Dict[str, Any]]) -> Dict[str, str]:
        if products is None or len(products) == 0:
            return {
                "answer": "비교할 상품 정보가 없습니다.",
                "status": "NOT_FOUND",
                "source": "fallback"
            }

        prompt = self._build_compare_prompt(products)
        llm_answer = self._generate_llm_answer(prompt)

        if llm_answer is not None and llm_answer.strip() != "":
            return {
                "answer": llm_answer.strip(),
                "status": "COMPARE",
                "source": "llm"
            }

        return {
            "answer": self._create_fallback_compare_summary(products),
            "status": "COMPARE",
            "source": "fallback"
        }

    # =========================================================
    # AI 맞춤 상품 추천
    # =========================================================

    def create_persona_recommend(self, request: Dict[str, Any]) -> Dict[str, Any]:
        products = request.get("products") or []

        if len(products) == 0:
            return {
                "summary": "추천 후보 상품 정보가 없습니다.",
                "status": "NOT_FOUND",
                "recommendedProducts": [],
                "source": "fallback"
            }

        scored_products = []

        for product in products:
            if not isinstance(product, dict):
                continue

            age = self._get_request_int(request, "age")

            if not self._is_age_available(product, age):
                continue

            scored_products.append(
                self._score_product_for_recommend(product, request)
            )

        scored_products.sort(key=lambda item: item["rawScore"], reverse=True)

        self._normalize_fit_scores(scored_products)

        recommended_products = []

        for scored in scored_products[:3]:
            product = scored["product"]

            reason = self._create_recommend_reason(
                product=product,
                request=request,
                fit_percent=scored["fitPercent"],
                benefit_chance_percent=scored["benefitChancePercent"],
                evidence=scored["evidence"]
            )

            recommended_products.append({
                "productNo": self.get_int(product, "product_no", "productNo"),
                "productName": self.get_text(product, "product_name", "productName"),
                "productType": self.get_text(product, "product_type", "productType"),
                "subtitle": self.get_text(product, "subtitle", "subTitle"),

                "minInterestRate": self.get_float(product, "min_interest_rate", "minInterestRate"),
                "maxInterestRate": self.get_float(product, "max_interest_rate", "maxInterestRate"),

                "minJoinAmount": self.get_int(product, "min_join_amount", "minJoinAmount"),
                "maxJoinAmount": self.get_int(product, "max_join_amount", "maxJoinAmount"),

                "branchJoinYn": self.get_yn(product, "branch_join_yn", "branchJoinYn"),
                "internetJoinYn": self.get_yn(product, "internet_join_yn", "internetJoinYn"),
                "mobileJoinYn": self.get_yn(product, "mobile_join_yn", "mobileJoinYn"),

                "score": scored["fitPercent"],
                "fitPercent": scored["fitPercent"],
                "benefitChancePercent": scored["benefitChancePercent"],

                "reason": reason,
                "evidence": scored["evidence"],
                "detailUrl": self._get_detail_url(product)
            })

        return {
            "summary": self._create_recommend_summary(request, recommended_products),
            "status": "FOUND" if recommended_products else "NOT_FOUND",
            "recommendedProducts": recommended_products,
            "source": "llm" if self._has_valid_api_key() else "fallback"
        }

    def _score_product_for_recommend(self, product: Dict[str, Any], request: Dict[str, Any]) -> Dict[str, Any]:
        raw_score = 45
        benefit_chance = 45
        evidence = []

        purpose = self._get_request_text(request, "purpose", "MAKE_MONEY").upper()
        preferred_product_type = self._get_request_text(request, "preferredProductType", "ALL").upper()
        preferred_channel = self._get_request_text(request, "preferredChannel", "ALL").upper()
        interest_conditions = self._get_request_list(request, "interestConditions")

        product_type = self.get_text(product, "product_type", "productType").upper()
        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")

        # 1. 선호 상품 유형
        if preferred_product_type == "ALL":
            raw_score += 4
        elif preferred_product_type == product_type:
            raw_score += 16
            evidence.append("선호 상품 유형 일치")
        else:
            raw_score -= 18
            evidence.append("선호 상품 유형과 다름")

        # 2. 가입 목적
        if purpose == "MAKE_MONEY":
            if product_type == "SAVINGS":
                raw_score += 18
                evidence.append("목돈 만들기 목적에 적합")
            else:
                raw_score -= 8

        elif purpose == "ROLL_MONEY":
            if product_type == "DEPOSIT":
                raw_score += 18
                evidence.append("목돈 굴리기 목적에 적합")
            else:
                raw_score -= 8

        elif purpose == "HIGH_RATE":
            raw_score += self._calculate_rate_score(max_rate, 6)
            evidence.append("고금리 우선 조건 반영")

        elif purpose == "EMERGENCY":
            min_join_amount = self.get_int(product, "min_join_amount", "minJoinAmount")

            if min_join_amount <= 10000:
                raw_score += 14
                evidence.append("비상금 목적에 맞는 소액 시작 가능")
            else:
                raw_score -= 6

        # 3. 금리
        raw_score += self._calculate_rate_score(max_rate, 4)
        evidence.append(f"최고금리 연 {max_rate:.2f}%")

        # 4. 금액 조건
        available_amount = self._get_available_amount_for_product(product, request)
        min_join_amount = self.get_int(product, "min_join_amount", "minJoinAmount")

        if available_amount > 0:
            if min_join_amount <= 0 or min_join_amount <= available_amount:
                raw_score += 12
                evidence.append("가입 가능 금액 조건 충족")
            else:
                raw_score -= 22
                evidence.append("최소 가입금액 확인 필요")

        if min_join_amount <= 10000:
            raw_score += 5

            if "LOW_AMOUNT" in interest_conditions:
                raw_score += 11
                evidence.append("소액 시작 가능")

        elif min_join_amount >= 1000000:
            if purpose in ("MAKE_MONEY", "EMERGENCY"):
                raw_score -= 8

        # 5. 가입 기간
        period_months = self._get_request_int(request, "periodMonths")

        if period_months > 0:
            if self._is_period_available(product, period_months):
                raw_score += 7
                evidence.append("희망 가입 기간 조건과 유사")
            else:
                raw_score -= 9
                evidence.append("희망 가입 기간과 차이 있음")

        # 6. 가입 채널
        if preferred_channel == "MOBILE":
            if self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y":
                raw_score += 14
                benefit_chance += 9
                evidence.append("모바일 가입 가능")
            else:
                raw_score -= 12
                evidence.append("모바일 가입 불가")

        elif preferred_channel == "INTERNET":
            if self.get_yn(product, "internet_join_yn", "internetJoinYn") == "Y":
                raw_score += 10
                evidence.append("인터넷 가입 가능")
            else:
                raw_score -= 8

        elif preferred_channel == "BRANCH":
            if self.get_yn(product, "branch_join_yn", "branchJoinYn") == "Y":
                raw_score += 10
                evidence.append("영업점 가입 가능")
            else:
                raw_score -= 8

        elif preferred_channel == "ALL":
            if self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y":
                raw_score += 4
                evidence.append("모바일 가입 가능")

        # 7. 관심 조건
        if "HIGH_RATE" in interest_conditions:
            raw_score += self._calculate_rate_score(max_rate, 3)

        if "MOBILE" in interest_conditions:
            if self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y":
                raw_score += 8
                benefit_chance += 6
                evidence.append("모바일 선호 조건 반영")
            else:
                raw_score -= 5

        if "PROTECTION" in interest_conditions:
            if self.get_yn(product, "depositor_protection_yn", "depositorProtectionYn") == "Y":
                raw_score += 6
                evidence.append("예금자보호 대상")
            else:
                raw_score -= 4

        if "PREFERENTIAL_RATE" in interest_conditions:
            if self._has_preferential_condition(product):
                raw_score += 9
                benefit_chance += 12
                evidence.append("우대조건 확인 가능")
            else:
                raw_score -= 6
                evidence.append("우대조건 확인 필요")

        benefit_chance += self._calculate_benefit_chance_bonus(product, request)
        benefit_chance = self._clamp(benefit_chance, 28, 95)

        raw_score = self._clamp(raw_score, 1, 140)
        evidence = self._unique(evidence)

        if not evidence:
            evidence.append("조건 기반 추천")

        return {
            "product": product,
            "rawScore": raw_score,
            "fitPercent": raw_score,
            "benefitChancePercent": benefit_chance,
            "evidence": evidence
        }

    def _normalize_fit_scores(self, scored_products: List[Dict[str, Any]]) -> None:
        if not scored_products:
            return

        top_raw_score = scored_products[0]["rawScore"]

        for index, item in enumerate(scored_products):
            raw_gap = max(0, top_raw_score - item["rawScore"])
            rank_penalty = index * 4

            normalized = 92 - raw_gap - rank_penalty

            if index == 0 and item["rawScore"] >= 95:
                normalized += 3

            if index == 0:
                normalized = self._clamp(normalized, 88, 96)
            elif index == 1:
                normalized = self._clamp(normalized, 80, 91)
            elif index == 2:
                normalized = self._clamp(normalized, 72, 87)
            else:
                normalized = self._clamp(normalized, 50, 82)

            item["fitPercent"] = normalized

    def _create_recommend_reason(
        self,
        product: Dict[str, Any],
        request: Dict[str, Any],
        fit_percent: int,
        benefit_chance_percent: int,
        evidence: List[str]
    ) -> str:
        prompt = self._build_recommend_reason_prompt(
            product=product,
            request=request,
            fit_percent=fit_percent,
            benefit_chance_percent=benefit_chance_percent,
            evidence=evidence
        )

        llm_answer = self._generate_llm_answer(prompt)

        if llm_answer is not None and llm_answer.strip() != "":
            return llm_answer.strip()

        return self._create_fallback_recommend_reason(
            product=product,
            request=request,
            fit_percent=fit_percent,
            benefit_chance_percent=benefit_chance_percent
        )

    def _build_recommend_reason_prompt(
        self,
        product: Dict[str, Any],
        request: Dict[str, Any],
        fit_percent: int,
        benefit_chance_percent: int,
        evidence: List[str]
    ) -> str:
        product_name = self.get_text(product, "product_name", "productName")
        product_type = self._get_product_type_label(product)
        evidence_text = ", ".join(evidence) if evidence else "조건 기반 추천"

        return f"""
너는 BNK 부산은행 예금/적금 상품 추천을 도와주는 금융 상담 AI다.
아래 고객 조건과 상품 정보를 참고해서 고객에게 보여줄 추천 이유를 한국어로 작성해라.

작성 조건:
- 2~3문장으로 짧게 작성한다.
- 고객이 이해하기 쉬운 은행 앱/웹 안내 문구처럼 작성한다.
- 금리, 가입 채널, 추천 근거를 자연스럽게 언급한다.
- 과장된 표현이나 확정적인 투자 조언은 피한다.
- product_no, API, DTO, 내부 점수 같은 개발자 용어는 쓰지 않는다.
- 마지막에는 가입 전 상품설명서와 우대조건 확인이 필요하다는 뉘앙스를 포함한다.

[고객 조건]
나이: {self._get_request_int(request, "age")}
현재 사용 가능 금액: {self._get_request_int(request, "balance")}
월 납입 가능 금액: {self._get_request_int(request, "monthlyAmount")}
희망 가입기간: {self._get_request_int(request, "periodMonths")}개월
가입 목적: {self._get_purpose_label(self._get_request_text(request, "purpose", "MAKE_MONEY"))}
선호 상품 유형: {self._get_request_text(request, "preferredProductType", "ALL")}
선호 가입 채널: {self._get_request_text(request, "preferredChannel", "ALL")}
관심 조건: {", ".join(self._get_request_list(request, "interestConditions"))}

[상품 정보]
상품명: {product_name}
상품유형: {product_type}
부제목: {self.get_text(product, "subtitle")}
최저금리: 연 {self.get_float(product, "min_interest_rate", "minInterestRate")}%
최고금리: 연 {self.get_float(product, "max_interest_rate", "maxInterestRate")}%
최소가입금액: {self._format_money(self.get_int(product, "min_join_amount", "minJoinAmount"))}
최대가입금액: {self._format_money(self.get_int(product, "max_join_amount", "maxJoinAmount"))}
가입기간: {self.get_int(product, "min_term_months", "minTermMonths")}개월 ~ {self.get_int(product, "max_term_months", "maxTermMonths")}개월
가입방법: {self.get_text(product, "join_method_desc", "joinMethodDesc")}
모바일 가입 가능 여부: {self.get_yn(product, "mobile_join_yn", "mobileJoinYn")}
인터넷 가입 가능 여부: {self.get_yn(product, "internet_join_yn", "internetJoinYn")}
영업점 가입 가능 여부: {self.get_yn(product, "branch_join_yn", "branchJoinYn")}
우대금리 요약: {self.get_text(product, "preferential_rate_summary", "preferentialRateSummary")}
우대조건/가입조건: {self.get_text(product, "condition_note", "conditionNote")}
예금자보호 여부: {self.get_yn(product, "depositor_protection_yn", "depositorProtectionYn")}

[계산 결과]
적합도: {fit_percent}%
우대조건 충족 가능성: {benefit_chance_percent}%
추천 근거: {evidence_text}
""".strip()

    def _create_fallback_recommend_reason(
        self,
        product: Dict[str, Any],
        request: Dict[str, Any],
        fit_percent: int,
        benefit_chance_percent: int
    ) -> str:
        product_name = self.get_text(product, "product_name", "productName")
        product_type = self._get_product_type_label(product)
        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")

        reason_parts = [
            f"{product_name}은/는 {product_type} 상품이며, 입력하신 조건 기준 적합도 {fit_percent}%로 추천할 수 있습니다."
        ]

        purpose = self._get_request_text(request, "purpose", "MAKE_MONEY").upper()
        product_type_code = self.get_text(product, "product_type", "productType").upper()

        if purpose == "MAKE_MONEY" and product_type_code == "SAVINGS":
            reason_parts.append("매월 꾸준히 저축해 목돈을 만들려는 목적과 잘 맞는 상품입니다.")
        elif purpose == "ROLL_MONEY" and product_type_code == "DEPOSIT":
            reason_parts.append("보유한 목돈을 일정 기간 운용하려는 목적과 잘 맞는 상품입니다.")
        elif purpose == "HIGH_RATE":
            reason_parts.append(f"최고금리 연 {max_rate:.2f}% 기준으로 금리 조건을 비교해볼 만합니다.")
        elif purpose == "EMERGENCY":
            reason_parts.append("비상금 마련 목적이라면 가입금액과 중도해지 조건을 함께 확인해볼 필요가 있습니다.")
        else:
            reason_parts.append("금리와 가입 조건을 함께 비교해볼 만한 상품입니다.")

        if self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y":
            reason_parts.append("모바일 가입이 가능해 비대면 가입을 선호하는 고객에게 접근성이 좋습니다.")
        elif self.get_yn(product, "internet_join_yn", "internetJoinYn") == "Y":
            reason_parts.append("인터넷 가입이 가능해 영업점 방문 없이 가입을 검토할 수 있습니다.")
        elif self.get_yn(product, "branch_join_yn", "branchJoinYn") == "Y":
            reason_parts.append("영업점 가입이 가능해 상담을 받고 가입하려는 고객에게 적합합니다.")

        reason_parts.append(
            f"우대조건 충족 가능성은 {benefit_chance_percent}% 수준으로 계산되었으며, 가입 전 세부 우대조건과 상품설명서를 확인해 주세요."
        )

        return " ".join(reason_parts)

    def _create_recommend_summary(self, request: Dict[str, Any], items: List[Dict[str, Any]]) -> str:
        if not items:
            return "입력한 조건에 맞는 추천 상품을 찾지 못했습니다. 금액이나 상품 유형 조건을 조금 완화해 다시 시도해 주세요."

        purpose_label = self._get_purpose_label(self._get_request_text(request, "purpose", "MAKE_MONEY"))
        top_item = items[0]

        return (
            f"{purpose_label} 기준으로 {top_item.get('productName')}을/를 가장 우선 추천합니다. "
            f"적합도는 {top_item.get('fitPercent')}%, "
            f"우대조건 충족 가능성은 {top_item.get('benefitChancePercent')}%로 계산되었습니다. "
            f"금리·가입금액·가입채널·관심 조건을 함께 반영했습니다."
        )

    # =========================================================
    # 요약/비교 프롬프트
    # =========================================================

    def _build_product_summary_prompt(self, product: Dict[str, Any]) -> str:
        product_name = self.get_text(product, "product_name", "productName")
        product_type = self._get_product_type_label(product)
        min_rate = self.get_float(product, "min_interest_rate", "minInterestRate")
        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")

        return f"""
너는 BNK 부산은행 예금/적금 상품을 고객에게 쉽게 설명해주는 금융 상담 AI다.
아래 상품 하나를 고객 화면에 표시할 수 있도록 한국어로 요약해라.

작성 조건:
- 2~3문장으로 짧게 작성한다.
- 상품 유형, 금리, 가입 방법, 우대조건을 중심으로 설명한다.
- 장점만 말하지 말고 확인해야 할 점도 자연스럽게 포함한다.
- 확정적인 투자 조언처럼 말하지 말고 참고용 안내처럼 말한다.
- product_no, API, DTO, 내부 필드명 같은 개발자 용어는 절대 쓰지 않는다.
- 고객에게 보이는 은행 앱/웹 안내 문구처럼 작성한다.

[상품 정보]
상품명: {product_name}
상품유형: {product_type}
최저금리: 연 {min_rate}%
최고금리: 연 {max_rate}%
이자지급방식: {self.get_text(product, "interest_payment_type", "interestPaymentType")}
이자계산방식: {self.get_text(product, "interest_calc_type", "interestCalcType")}
가입방법 설명: {self.get_text(product, "join_method_desc", "joinMethodDesc")}
영업점 가입 가능여부: {self.get_text(product, "branch_join_yn", "branchJoinYn")}
인터넷 가입 가능여부: {self.get_text(product, "internet_join_yn", "internetJoinYn")}
모바일 가입 가능여부: {self.get_text(product, "mobile_join_yn", "mobileJoinYn")}
최소 가입금액: {self._format_money(self.get_int(product, "min_join_amount", "minJoinAmount"))}
최대 가입금액: {self._format_money(self.get_int(product, "max_join_amount", "maxJoinAmount"))}
최소 가입기간: {self.get_int(product, "min_term_months", "minTermMonths")}개월
최대 가입기간: {self.get_int(product, "max_term_months", "maxTermMonths")}개월
우대조건/가입조건: {self.get_text(product, "condition_note", "conditionNote")}
예금자보호 여부: {self.get_text(product, "depositor_protection_yn", "depositorProtectionYn")}
""".strip()

    def _build_compare_prompt(self, products: List[Dict[str, Any]]) -> str:
        product_lines = []

        for index, product in enumerate(products, start=1):
            product_name = self.get_text(product, "product_name", "productName")
            product_type = self._get_product_type_label(product)
            min_rate = self.get_float(product, "min_interest_rate", "minInterestRate")
            max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")

            product_lines.append(f"""
[상품 {index}]
상품명: {product_name}
상품유형: {product_type}
최저금리: 연 {min_rate}%
최고금리: 연 {max_rate}%
가입방법 설명: {self.get_text(product, "join_method_desc", "joinMethodDesc")}
영업점 가입 가능여부: {self.get_text(product, "branch_join_yn", "branchJoinYn")}
인터넷 가입 가능여부: {self.get_text(product, "internet_join_yn", "internetJoinYn")}
모바일 가입 가능여부: {self.get_text(product, "mobile_join_yn", "mobileJoinYn")}
최소 가입금액: {self._format_money(self.get_int(product, "min_join_amount", "minJoinAmount"))}
최대 가입금액: {self._format_money(self.get_int(product, "max_join_amount", "maxJoinAmount"))}
최소 가입기간: {self.get_int(product, "min_term_months", "minTermMonths")}개월
최대 가입기간: {self.get_int(product, "max_term_months", "maxTermMonths")}개월
우대조건/가입조건: {self.get_text(product, "condition_note", "conditionNote")}
예금자보호 여부: {self.get_text(product, "depositor_protection_yn", "depositorProtectionYn")}
""".strip())

        joined_products = "\n\n".join(product_lines)

        return f"""
너는 BNK 부산은행 예금/적금 상품을 비교해주는 금융 상담 AI다.
아래 상품들을 비교해서 고객이 이해하기 쉬운 한국어 요약을 작성해라.

작성 조건:
- 4~6문장 정도로 작성한다.
- 금리 차이, 가입 방식, 상품 유형 차이를 중심으로 설명한다.
- 어떤 목적의 고객에게 어떤 상품이 더 적합한지 말한다.
- 금리만 보고 선택하지 않도록 가입금액, 가입기간, 우대조건, 가입채널도 함께 언급한다.
- 확정적인 투자 조언처럼 말하지 말고 참고용 안내처럼 말한다.
- product_no, API, DTO, 내부 필드명 같은 개발자 용어는 절대 쓰지 않는다.
- 고객에게 보이는 은행 앱/웹 안내 문구처럼 작성한다.
- 마지막에는 상품설명서와 우대조건 확인이 필요하다는 안내를 자연스럽게 포함한다.

[비교 상품 목록]
{joined_products}
""".strip()

    # =========================================================
    # LLM 호출
    # =========================================================

    def _generate_llm_answer(self, prompt: str) -> Optional[str]:
        if prompt is None or prompt.strip() == "":
            return None

        if not self._has_valid_api_key():
            return None

        body = {
            "model": self.model,
            "instructions": (
                "너는 BNK 부산은행 예금/적금 상품을 고객에게 쉽게 설명하는 금융 상담 AI다. "
                "항상 한국어로 답변한다. "
                "고객에게 보이는 문장으로 작성하고, 개발자 용어는 쓰지 않는다. "
                "확정적인 수익 보장 표현이나 과장된 표현은 피한다."
            ),
            "input": prompt,
            "max_output_tokens": 700
        }

        try:
            request = urllib.request.Request(
                url="https://api.openai.com/v1/responses",
                data=json.dumps(body).encode("utf-8"),
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {self.api_key}"
                },
                method="POST"
            )

            with urllib.request.urlopen(request, timeout=self.timeout_seconds) as response:
                response_text = response.read().decode("utf-8")
                response_body = json.loads(response_text)

            return self._extract_output_text(response_body)

        except urllib.error.HTTPError as error:
            try:
                error_body = error.read().decode("utf-8")
            except Exception:
                error_body = str(error)

            print(f"상품 AI LLM HTTP 호출 실패. error={error_body}")
            return None

        except Exception as error:
            print(f"상품 AI LLM 호출 실패. error={error}")
            return None

    def _extract_output_text(self, response_body: Dict[str, Any]) -> Optional[str]:
        if not response_body:
            return None

        output_text = response_body.get("output_text")

        if isinstance(output_text, str) and output_text.strip() != "":
            return output_text.strip()

        output = response_body.get("output")

        if not isinstance(output, list):
            return None

        texts = []

        for output_item in output:
            if not isinstance(output_item, dict):
                continue

            content = output_item.get("content")

            if not isinstance(content, list):
                continue

            for content_item in content:
                if not isinstance(content_item, dict):
                    continue

                text = content_item.get("text")

                if isinstance(text, str) and text.strip() != "":
                    texts.append(text.strip())

        result = "\n".join(texts).strip()

        if result == "":
            return None

        return result

    # =========================================================
    # fallback 요약/비교
    # =========================================================

    def _create_fallback_compare_summary(self, products: List[Dict[str, Any]]) -> str:
        if products is None or len(products) == 0:
            return "비교할 상품 정보가 없습니다."

        highest_max_rate_product = products[0]
        lowest_min_amount_product = products[0]
        easiest_mobile_product = None
        branch_only_product = None

        deposit_count = 0
        savings_count = 0

        for product in products:
            if self.get_float(product, "max_interest_rate", "maxInterestRate") > self.get_float(
                highest_max_rate_product,
                "max_interest_rate",
                "maxInterestRate"
            ):
                highest_max_rate_product = product

            if self.get_int(product, "min_join_amount", "minJoinAmount") < self.get_int(
                lowest_min_amount_product,
                "min_join_amount",
                "minJoinAmount"
            ):
                lowest_min_amount_product = product

            if self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y" and easiest_mobile_product is None:
                easiest_mobile_product = product

            if (
                self.get_yn(product, "branch_join_yn", "branchJoinYn") == "Y"
                and self.get_yn(product, "internet_join_yn", "internetJoinYn") != "Y"
                and self.get_yn(product, "mobile_join_yn", "mobileJoinYn") != "Y"
                and branch_only_product is None
            ):
                branch_only_product = product

            product_type = self.get_text(product, "product_type", "productType").upper()

            if product_type == "DEPOSIT":
                deposit_count += 1
            elif product_type == "SAVINGS":
                savings_count += 1

        summary = []

        base = f"선택한 상품은 총 {len(products)}개이며, "

        if deposit_count > 0 and savings_count > 0:
            base += "예금과 적금 상품이 함께 비교되고 있습니다."
        elif deposit_count > 0:
            base += "예금 상품 중심으로 비교되고 있습니다."
        elif savings_count > 0:
            base += "적금 상품 중심으로 비교되고 있습니다."
        else:
            base += "상품별 금리와 가입 조건을 기준으로 비교되고 있습니다."

        summary.append(base)

        highest_name = self.get_text(highest_max_rate_product, "product_name", "productName")
        highest_rate = self.get_float(highest_max_rate_product, "max_interest_rate", "maxInterestRate")

        summary.append(
            f"최고금리 기준으로는 {highest_name}이/가 연 {highest_rate}%로 가장 높습니다."
        )

        lowest_name = self.get_text(lowest_min_amount_product, "product_name", "productName")
        lowest_amount = self.get_int(lowest_min_amount_product, "min_join_amount", "minJoinAmount")

        summary.append(
            f"최소 가입금액 기준으로는 {lowest_name}이/가 {self._format_money(lowest_amount)}부터 가입 가능해 접근성이 좋습니다."
        )

        if easiest_mobile_product is not None:
            mobile_name = self.get_text(easiest_mobile_product, "product_name", "productName")
            summary.append(
                f"또한 {mobile_name}은/는 모바일 가입이 가능해 비대면 가입을 원하는 고객에게 적합합니다."
            )

        if branch_only_product is not None:
            branch_name = self.get_text(branch_only_product, "product_name", "productName")
            summary.append(
                f"{branch_name}은/는 영업점 중심 상품이므로 상담을 통한 가입을 선호하는 경우에 확인해볼 만합니다."
            )

        summary.append(
            "금리만 보고 선택하기보다는 가입금액, 가입기간, 우대조건, 가입채널을 함께 비교한 뒤 선택하는 것이 좋습니다."
        )

        return " ".join(summary)

    def _create_fallback_product_summary(self, product: Dict[str, Any]) -> str:
        product_type = self._get_product_type_label(product)
        product_name = self.get_text(product, "product_name", "productName")

        summary = []

        summary.append(f"{product_name}은/는 {product_type} 상품입니다.")
        summary.append(self._build_rate_point(product))
        summary.append(self._build_amount_point(product))
        summary.append(self._build_channel_point(product))
        summary.append(self._build_condition_point(product))

        return " ".join(summary)

    def _build_rate_point(self, product: Dict[str, Any]) -> str:
        min_rate = self.get_float(product, "min_interest_rate", "minInterestRate")
        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")
        gap = max_rate - min_rate

        text = ""

        if max_rate >= 5.0:
            text += f"최고금리가 연 {max_rate}% 수준이라 금리 매력이 강한 편입니다."
        elif max_rate >= 3.0:
            text += f"최고금리는 연 {max_rate}%로, 우대조건을 챙겼을 때 장점이 커지는 상품입니다."
        else:
            text += f"최고금리는 연 {max_rate}%로 비교적 안정적인 기본형 상품에 가깝습니다."

        if gap >= 1.0:
            text += " 최저금리와 최고금리 차이가 있어 우대조건 충족 여부가 중요합니다."
        else:
            text += " 최저금리와 최고금리 차이가 크지 않아 조건 변동 부담은 비교적 낮은 편입니다."

        return text

    def _build_amount_point(self, product: Dict[str, Any]) -> str:
        min_amount = self.get_int(product, "min_join_amount", "minJoinAmount")

        if min_amount <= 10000:
            return f"최소 가입금액이 {self._format_money(min_amount)}이라 소액으로 시작하기 좋습니다."

        if min_amount <= 1000000:
            return f"최소 가입금액은 {self._format_money(min_amount)}으로 일반적인 목돈 운용에 적합합니다."

        return f"최소 가입금액이 {self._format_money(min_amount)}이라 어느 정도 자금이 준비된 고객에게 더 적합합니다."

    def _build_channel_point(self, product: Dict[str, Any]) -> str:
        branch = self.get_yn(product, "branch_join_yn", "branchJoinYn") == "Y"
        internet = self.get_yn(product, "internet_join_yn", "internetJoinYn") == "Y"
        mobile = self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y"

        if mobile and not branch and not internet:
            return "가입채널은 모바일 중심이라 비대면 가입을 원하는 고객에게 잘 맞습니다."

        if mobile and (branch or internet):
            return "모바일 가입도 가능해서 접근성이 좋고, 다른 채널과 함께 선택지가 넓은 편입니다."

        if branch and not internet and not mobile:
            return "영업점 가입 중심 상품이라 직원 상담을 받고 가입하려는 고객에게 적합합니다."

        if internet and not mobile:
            return "인터넷 가입이 가능해 온라인으로 상품을 확인하고 가입하려는 경우에 활용하기 좋습니다."

        return "가입채널은 상품 안내에서 한 번 더 확인하는 것이 좋습니다."

    def _build_condition_point(self, product: Dict[str, Any]) -> str:
        condition = self.get_text(product, "condition_note", "conditionNote")

        if condition.strip() == "" or condition.strip() == "-":
            return "우대조건 정보가 많지 않으므로 기본금리와 가입조건을 중심으로 판단하면 좋습니다."

        if "비과세" in condition:
            return "조건에는 비과세 관련 내용이 포함되어 있어 세제 혜택 대상 여부를 함께 확인하는 것이 좋습니다."

        if "급여" in condition or "자동이체" in condition or "카드" in condition:
            return "우대조건은 급여, 자동이체, 카드 사용 같은 거래 실적과 연결될 수 있으므로 본인의 거래 패턴과 맞는지 확인해야 합니다."

        if len(condition) > 80:
            return "우대조건이 비교적 세부적인 편이므로 가입 전 조건 충족 가능성을 꼼꼼히 확인하는 것이 좋습니다."

        return "우대조건을 충족할 수 있다면 기본 조건보다 더 유리하게 활용할 수 있는 상품입니다."

    # =========================================================
    # 추천 보조 로직
    # =========================================================

    def _is_age_available(self, product: Dict[str, Any], age: int) -> bool:
        if age <= 0:
            return True

        min_age = self.get_int(product, "min_age", "minAge")
        max_age = self.get_int(product, "max_age", "maxAge")

        if min_age > 0 and age < min_age:
            return False

        return max_age <= 0 or age <= max_age

    def _is_period_available(self, product: Dict[str, Any], period_months: int) -> bool:
        min_term = self.get_int(product, "min_term_months", "minTermMonths")
        max_term = self.get_int(product, "max_term_months", "maxTermMonths")

        if min_term <= 0 and max_term <= 0:
            return True

        if min_term > 0 and period_months < min_term:
            return False

        return max_term <= 0 or period_months <= max_term

    def _get_available_amount_for_product(self, product: Dict[str, Any], request: Dict[str, Any]) -> int:
        product_type = self.get_text(product, "product_type", "productType").upper()

        if product_type == "SAVINGS":
            monthly_amount = self._get_request_int(request, "monthlyAmount")
            balance = self._get_request_int(request, "balance")
            return monthly_amount if monthly_amount > 0 else balance

        return self._get_request_int(request, "balance")

    def _calculate_rate_score(self, max_rate: float, multiplier: int) -> int:
        return min(round(max_rate * multiplier), 24)

    def _has_preferential_condition(self, product: Dict[str, Any]) -> bool:
        text = self._normalize_text(
            f"{self.get_text(product, 'preferential_rate_summary', 'preferentialRateSummary')} "
            f"{self.get_text(product, 'condition_note', 'conditionNote')} "
            f"{self.get_text(product, 'join_method_desc', 'joinMethodDesc')}"
        )

        return (
            "우대" in text
            or "급여" in text
            or "자동이체" in text
            or "카드" in text
            or "모바일" in text
            or "비대면" in text
        )

    def _calculate_benefit_chance_bonus(self, product: Dict[str, Any], request: Dict[str, Any]) -> int:
        bonus = 0

        text = self._normalize_text(
            f"{self.get_text(product, 'preferential_rate_summary', 'preferentialRateSummary')} "
            f"{self.get_text(product, 'condition_note', 'conditionNote')} "
            f"{self.get_text(product, 'join_method_desc', 'joinMethodDesc')}"
        )

        conditions = self._get_request_list(request, "interestConditions")

        if "모바일" in text or "비대면" in text:
            bonus += 8

        if "자동이체" in text:
            bonus += 6

        if "급여" in text:
            bonus += 4

        if "카드" in text:
            bonus += 4

        if "MOBILE" in conditions and self.get_yn(product, "mobile_join_yn", "mobileJoinYn") == "Y":
            bonus += 8

        if "LOW_AMOUNT" in conditions and self.get_int(product, "min_join_amount", "minJoinAmount") <= 10000:
            bonus += 8

        if "HIGH_RATE" in conditions and self.get_float(product, "max_interest_rate", "maxInterestRate") >= 3.0:
            bonus += 6

        if "PREFERENTIAL_RATE" in conditions and self._has_preferential_condition(product):
            bonus += 8

        return bonus

    # =========================================================
    # 공통 유틸
    # =========================================================

    def get_text(self, data: Dict[str, Any], *keys: str) -> str:
        value = self._get_value(data, *keys)

        if value is None:
            return ""

        return str(value).replace("null", "").strip()

    def get_int(self, data: Dict[str, Any], *keys: str) -> int:
        value = self._get_value(data, *keys)

        try:
            if value is None or value == "":
                return 0

            return int(float(value))

        except Exception:
            return 0

    def get_float(self, data: Dict[str, Any], *keys: str) -> float:
        value = self._get_value(data, *keys)

        try:
            if value is None or value == "":
                return 0.0

            return float(value)

        except Exception:
            return 0.0

    def get_yn(self, data: Dict[str, Any], *keys: str) -> str:
        value = self.get_text(data, *keys).upper()

        return "Y" if value == "Y" else "N"

    def _get_value(self, data: Dict[str, Any], *keys: str) -> Any:
        if data is None:
            return None

        for key in keys:
            if key in data:
                return data.get(key)

        return None

    def _get_request_text(self, request: Dict[str, Any], key: str, default: str = "") -> str:
        value = request.get(key)

        if value is None:
            return default

        return str(value).strip() or default

    def _get_request_int(self, request: Dict[str, Any], key: str) -> int:
        value = request.get(key)

        try:
            if value is None or value == "":
                return 0

            return int(float(value))

        except Exception:
            return 0

    def _get_request_list(self, request: Dict[str, Any], key: str) -> List[str]:
        value = request.get(key)

        if not isinstance(value, list):
            return []

        result = []

        for item in value:
            if item is None:
                continue

            text = str(item).strip().upper()

            if text:
                result.append(text)

        return result

    def _get_product_type_label(self, product: Dict[str, Any]) -> str:
        product_type = self.get_text(product, "product_type", "productType").upper()

        if product_type == "DEPOSIT":
            return "예금"

        if product_type == "SAVINGS":
            return "적금"

        return "금융상품"

    def _get_purpose_label(self, purpose: str) -> str:
        purpose = (purpose or "").upper()

        if purpose == "MAKE_MONEY":
            return "목돈 만들기 목적"

        if purpose == "ROLL_MONEY":
            return "목돈 굴리기 목적"

        if purpose == "HIGH_RATE":
            return "고금리 우선 조건"

        if purpose == "EMERGENCY":
            return "비상금 마련 목적"

        return "입력한 조건"

    def _get_detail_url(self, product: Dict[str, Any]) -> str:
        explicit_url = self.get_text(product, "detail_url", "detailUrl")

        if explicit_url:
            return explicit_url

        product_no = self.get_int(product, "product_no", "productNo")

        if product_no > 0:
            return f"/products/detail?product_no={product_no}"

        return "/products"

    def _format_money(self, amount: int) -> str:
        try:
            value = int(amount)
        except Exception:
            value = 0

        if value <= 0:
            return "제한 없음 또는 확인 필요"

        return f"{value:,}원"

    def _normalize_text(self, value: str) -> str:
        if value is None:
            return ""

        return str(value).replace("null", "").strip()

    def _unique(self, values: List[str]) -> List[str]:
        result = []

        for value in values:
            if value and value not in result:
                result.append(value)

        return result

    def _clamp(self, value: int, min_value: int, max_value: int) -> int:
        return max(min_value, min(max_value, value))

    def _has_valid_api_key(self) -> bool:
        return self.api_key is not None and self.api_key.strip() != "" and "*" not in self.api_key

    # =========================================================
    # secret.env 로드
    # =========================================================

    def _load_secret_env(self) -> None:
        env_path = os.getenv("BNK_SECRET_ENV_PATH", "secret.env")

        if not os.path.exists(env_path):
            return

        try:
            with open(env_path, "r", encoding="utf-8") as file:
                for line in file:
                    text = line.strip()

                    if not text or text.startswith("#") or "=" not in text:
                        continue

                    key, value = text.split("=", 1)
                    key = key.strip()
                    value = value.strip().strip('"').strip("'")

                    if key and key not in os.environ:
                        os.environ[key] = value

        except Exception as error:
            print(f"secret.env 로드 생략. error={error}")