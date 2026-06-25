import json
import os
import urllib.error
import urllib.request
from typing import Any, Dict, List, Optional


class ProductAiService:
    """
    상품 요약/비교/추천 AI 서비스.

    - OpenAI API 키가 있으면 LLM 호출
    - 키가 없거나 실패하면 fallback 문장 생성
    - Spring에서 넘긴 상품 DTO를 기반으로 동작
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
            or "gpt-4o-mini"
        )

        self.timeout_seconds = int(os.getenv("PRODUCT_AI_TIMEOUT_SECONDS", "20"))

    # ============================================================
    # 외부 공개 메서드
    # ============================================================

    def create_product_summary(self, product: Dict[str, Any]) -> Dict[str, Any]:
        product_name = self.get_text(product, "product_name", "productName")

        if not product_name:
            return {
                "answer": "요약할 상품 정보가 없습니다.",
                "status": "NOT_FOUND",
                "source": "fallback"
            }

        prompt = self._build_summary_prompt(product)
        generated = self._generate(prompt)

        if generated:
            return {
                "answer": generated,
                "status": "FOUND",
                "source": "openai"
            }

        return {
            "answer": self._fallback_product_summary(product),
            "status": "FOUND",
            "source": "fallback"
        }

    def create_compare_summary(self, products: List[Dict[str, Any]]) -> Dict[str, Any]:
        products = products or []

        if len(products) == 0:
            return {
                "answer": "비교할 상품 정보가 없습니다.",
                "status": "NOT_FOUND",
                "source": "fallback"
            }

        if len(products) == 1:
            summary = self.create_product_summary(products[0])
            return {
                "answer": summary["answer"],
                "status": "SINGLE_FOUND",
                "source": summary["source"]
            }

        prompt = self._build_compare_prompt(products)
        generated = self._generate(prompt)

        if generated:
            return {
                "answer": generated,
                "status": "FOUND",
                "source": "openai"
            }

        return {
            "answer": self._fallback_compare_summary(products),
            "status": "FOUND",
            "source": "fallback"
        }

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
            score_result = self._score_product(product, request)
            scored_products.append({
                "product": product,
                "score": score_result["score"],
                "fitPercent": score_result["fitPercent"],
                "benefitChancePercent": score_result["benefitChancePercent"],
                "evidence": score_result["evidence"]
            })

        scored_products.sort(
            key=lambda item: (
                item["fitPercent"],
                self.get_float(item["product"], "max_interest_rate", "maxInterestRate")
            ),
            reverse=True
        )

        top_items = scored_products[:3]
        recommended_products = []

        for index, item in enumerate(top_items, start=1):
            product = item["product"]
            reason = self._create_recommend_reason(
                product=product,
                request=request,
                rank=index,
                fit_percent=item["fitPercent"],
                benefit_chance_percent=item["benefitChancePercent"],
                evidence=item["evidence"]
            )

            recommended_products.append({
                "productNo": self.get_int(product, "product_no", "productNo"),
                "productName": self.get_text(product, "product_name", "productName"),
                "productType": self.get_text(product, "product_type", "productType"),
                "subtitle": self.get_text(product, "subtitle"),
                "minInterestRate": self.get_float(product, "min_interest_rate", "minInterestRate"),
                "maxInterestRate": self.get_float(product, "max_interest_rate", "maxInterestRate"),
                "minJoinAmount": self.get_int(product, "min_join_amount", "minJoinAmount"),
                "maxJoinAmount": self.get_int(product, "max_join_amount", "maxJoinAmount"),
                "branchJoinYn": self.get_text(product, "branch_join_yn", "branchJoinYn", default="N"),
                "internetJoinYn": self.get_text(product, "internet_join_yn", "internetJoinYn", default="N"),
                "mobileJoinYn": self.get_text(product, "mobile_join_yn", "mobileJoinYn", default="N"),
                "score": item["score"],
                "fitPercent": item["fitPercent"],
                "benefitChancePercent": item["benefitChancePercent"],
                "reason": reason,
                "evidence": item["evidence"],
                "detailUrl": f"/products/detail?product_no={self.get_int(product, 'product_no', 'productNo')}"
            })

        summary = self._create_recommend_summary(
            request=request,
            recommended_products=recommended_products
        )

        return {
            "summary": summary,
            "status": "FOUND",
            "recommendedProducts": recommended_products,
            "source": "openai" if self._has_valid_api_key() else "fallback"
        }

    # ============================================================
    # Prompt
    # ============================================================

    def _build_summary_prompt(self, product: Dict[str, Any]) -> str:
        return f"""
너는 BNK 부산은행 예적금 상품 상담 직원이다.
아래 상품 정보를 고객이 이해하기 쉽게 4~5문장으로 요약해라.

조건:
- 내부 필드명, product_no, API, JSON 같은 개발 용어는 말하지 않는다.
- 금리, 가입방법, 가입금액, 가입기간, 우대조건을 자연스럽게 설명한다.
- 과장하지 말고 고객 안내문처럼 작성한다.

상품 정보:
{json.dumps(product, ensure_ascii=False)}
""".strip()

    def _build_compare_prompt(self, products: List[Dict[str, Any]]) -> str:
        return f"""
너는 BNK 부산은행 예적금 상품 비교 상담 직원이다.
아래 상품들을 비교해서 고객이 선택하기 쉽게 요약해라.

조건:
- 5~7문장 정도로 작성한다.
- 최고금리, 최소 가입금액, 가입채널, 가입기간, 우대조건을 비교한다.
- 특정 상품 하나만 무조건 좋다고 하지 말고, 고객 상황별로 어울리는 상품을 설명한다.
- 내부 필드명, product_no, API, JSON 같은 개발 용어는 말하지 않는다.

상품 목록:
{json.dumps(products, ensure_ascii=False)}
""".strip()

    def _build_reason_prompt(
        self,
        product: Dict[str, Any],
        request: Dict[str, Any],
        fit_percent: int,
        benefit_chance_percent: int,
        evidence: List[str]
    ) -> str:
        return f"""
너는 BNK 부산은행 모바일 상품 추천 상담 직원이다.
아래 고객 조건과 상품 정보를 바탕으로 추천 이유를 3~4문장으로 작성해라.

조건:
- 고객에게 보이는 문장으로만 작성한다.
- 내부 필드명, 점수 계산 방식, API, JSON은 말하지 않는다.
- 적합도 {fit_percent}%와 우대조건 가능성 {benefit_chance_percent}%는 자연스럽게 반영한다.
- 상품의 금리, 가입채널, 가입금액, 가입기간 장점을 설명한다.

고객 조건:
{json.dumps(request, ensure_ascii=False)}

추천 상품:
{json.dumps(product, ensure_ascii=False)}

추천 근거:
{json.dumps(evidence, ensure_ascii=False)}
""".strip()

    # ============================================================
    # Fallback 문장
    # ============================================================

    def _fallback_product_summary(self, product: Dict[str, Any]) -> str:
        name = self.get_text(product, "product_name", "productName", default="해당 상품")
        product_type = self._product_type_label(product)
        min_rate = self.get_float(product, "min_interest_rate", "minInterestRate")
        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")
        min_amount = self.get_int(product, "min_join_amount", "minJoinAmount")
        max_amount = self.get_int(product, "max_join_amount", "maxJoinAmount")
        join_method = self._join_method_text(product)
        condition_note = self.get_text(product, "condition_note", "conditionNote")

        answer = (
            f"{name}은/는 {product_type} 상품입니다. "
            f"금리는 연 {min_rate:.1f}%부터 최고 연 {max_rate:.1f}%까지 적용될 수 있습니다. "
        )

        if min_amount > 0:
            answer += f"최소 가입금액은 {self._format_money(min_amount)}부터입니다. "

        if max_amount > 0:
            answer += f"최대 가입금액은 {self._format_money(max_amount)}까지 확인됩니다. "

        if join_method:
            answer += f"가입은 {join_method}을 통해 가능합니다. "

        if condition_note:
            answer += f"우대조건이나 가입조건은 {condition_note} 내용을 함께 확인하는 것이 좋습니다. "

        answer += "금리만 보기보다는 가입기간, 가입금액, 우대조건을 함께 비교해 선택하는 것이 좋습니다."

        return answer

    def _fallback_compare_summary(self, products: List[Dict[str, Any]]) -> str:
        highest = max(
            products,
            key=lambda product: self.get_float(product, "max_interest_rate", "maxInterestRate")
        )
        lowest_amount = min(
            products,
            key=lambda product: self.get_int(product, "min_join_amount", "minJoinAmount")
        )

        deposit_count = 0
        savings_count = 0

        for product in products:
            product_type = self.get_text(product, "product_type", "productType").upper()

            if product_type == "DEPOSIT":
                deposit_count += 1
            elif product_type == "SAVINGS":
                savings_count += 1

        summary = f"선택한 상품은 총 {len(products)}개이며, "

        if deposit_count > 0 and savings_count > 0:
            summary += "예금과 적금 상품이 함께 비교되고 있습니다. "
        elif deposit_count > 0:
            summary += "예금 상품 중심으로 비교되고 있습니다. "
        elif savings_count > 0:
            summary += "적금 상품 중심으로 비교되고 있습니다. "

        summary += (
            f"최고금리 기준으로는 {self.get_text(highest, 'product_name', 'productName')}이/가 "
            f"연 {self.get_float(highest, 'max_interest_rate', 'maxInterestRate'):.1f}%로 가장 높습니다. "
        )

        summary += (
            f"최소 가입금액 기준으로는 {self.get_text(lowest_amount, 'product_name', 'productName')}이/가 "
            f"{self._format_money(self.get_int(lowest_amount, 'min_join_amount', 'minJoinAmount'))}부터 가입 가능해 접근성이 좋습니다. "
        )

        mobile_products = [
            product for product in products
            if self.get_text(product, "mobile_join_yn", "mobileJoinYn").upper() == "Y"
        ]

        if mobile_products:
            summary += (
                f"또한 {self.get_text(mobile_products[0], 'product_name', 'productName')}은/는 "
                "모바일 가입이 가능해 비대면 가입을 원하는 고객에게 적합합니다. "
            )

        summary += "금리만 보고 선택하기보다는 가입금액, 가입기간, 우대조건, 가입채널을 함께 비교한 뒤 선택하는 것이 좋습니다."

        return summary

    def _create_recommend_reason(
        self,
        product: Dict[str, Any],
        request: Dict[str, Any],
        rank: int,
        fit_percent: int,
        benefit_chance_percent: int,
        evidence: List[str]
    ) -> str:
        prompt = self._build_reason_prompt(
            product=product,
            request=request,
            fit_percent=fit_percent,
            benefit_chance_percent=benefit_chance_percent,
            evidence=evidence
        )

        generated = self._generate(prompt)

        if generated:
            return generated

        name = self.get_text(product, "product_name", "productName", default="해당 상품")
        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")
        channel = self._join_method_text(product)

        reason = (
            f"{name}은/는 입력하신 조건 기준 적합도 {fit_percent}%로 추천할 수 있는 상품입니다. "
            f"최고금리 연 {max_rate:.1f}%를 기준으로 금리 조건을 비교해볼 만합니다. "
        )

        if channel:
            reason += f"가입은 {channel}을 통해 가능해 선호하시는 가입 방식과도 비교해볼 수 있습니다. "

        reason += (
            f"우대조건 충족 가능성은 {benefit_chance_percent}% 수준으로 계산되었으며, "
            "가입 전 세부 우대조건과 상품설명서를 함께 확인하는 것이 좋습니다."
        )

        return reason

    def _create_recommend_summary(
        self,
        request: Dict[str, Any],
        recommended_products: List[Dict[str, Any]]
    ) -> str:
        if not recommended_products:
            return "조건에 맞는 추천 상품을 찾지 못했습니다."

        first = recommended_products[0]
        product_name = first.get("productName", "추천 상품")
        fit_percent = first.get("fitPercent", 0)
        benefit_percent = first.get("benefitChancePercent", 0)

        purpose = str(request.get("purpose") or "MAKE_MONEY").upper()

        if purpose == "ROLL_MONEY":
            purpose_text = "목돈 굴리기"
        elif purpose == "HIGH_RATE":
            purpose_text = "고금리 우선"
        elif purpose == "EMERGENCY":
            purpose_text = "비상금 관리"
        else:
            purpose_text = "목돈 만들기"

        return (
            f"{purpose_text} 목적 기준으로 {product_name}을/를 가장 우선 추천합니다. "
            f"적합도는 {fit_percent}%, 우대조건 충족 가능성은 {benefit_percent}%로 계산되었습니다. "
            "금리, 가입금액, 가입채널, 관심 조건을 함께 반영했습니다."
        )

    # ============================================================
    # 점수 계산
    # ============================================================

    def _score_product(self, product: Dict[str, Any], request: Dict[str, Any]) -> Dict[str, Any]:
        score = 0
        evidence = []

        max_rate = self.get_float(product, "max_interest_rate", "maxInterestRate")
        min_amount = self.get_int(product, "min_join_amount", "minJoinAmount")
        max_amount = self.get_int(product, "max_join_amount", "maxJoinAmount")
        min_term = self.get_int(product, "min_term_months", "minTermMonths")
        max_term = self.get_int(product, "max_term_months", "maxTermMonths")

        product_type = self.get_text(product, "product_type", "productType").upper()
        preferred_type = str(request.get("preferredProductType") or "ALL").upper()
        preferred_channel = str(request.get("preferredChannel") or "ALL").upper()
        purpose = str(request.get("purpose") or "MAKE_MONEY").upper()
        interest_conditions = [
            str(condition).upper()
            for condition in request.get("interestConditions", [])
            if condition
        ]

        monthly_amount = int(request.get("monthlyAmount") or 0)
        balance = int(request.get("balance") or 0)
        period_months = int(request.get("periodMonths") or 0)

        if preferred_type == "ALL" or preferred_type == product_type:
            score += 18
            evidence.append("선호 상품 유형 일치")

        if max_rate >= 4.0:
            score += 22
            evidence.append(f"최고금리 연 {max_rate:.1f}%")
        elif max_rate >= 3.0:
            score += 16
            evidence.append(f"금리 조건 양호")
        else:
            score += 8

        if purpose == "MAKE_MONEY" and product_type == "SAVINGS":
            score += 14
            evidence.append("목돈 만들기 목적에 적금 유형 적합")
        elif purpose == "ROLL_MONEY" and product_type == "DEPOSIT":
            score += 14
            evidence.append("목돈 굴리기 목적에 예금 유형 적합")
        elif purpose == "HIGH_RATE":
            score += 10
            evidence.append("고금리 우선 조건 반영")

        if preferred_channel == "MOBILE" and self.get_text(product, "mobile_join_yn", "mobileJoinYn").upper() == "Y":
            score += 13
            evidence.append("모바일 가입 가능")
        elif preferred_channel == "INTERNET" and self.get_text(product, "internet_join_yn", "internetJoinYn").upper() == "Y":
            score += 13
            evidence.append("인터넷 가입 가능")
        elif preferred_channel == "BRANCH" and self.get_text(product, "branch_join_yn", "branchJoinYn").upper() == "Y":
            score += 13
            evidence.append("영업점 가입 가능")
        elif preferred_channel == "ALL":
            score += 8

        target_amount = monthly_amount if product_type == "SAVINGS" else balance

        if target_amount > 0:
            if min_amount <= target_amount and (max_amount == 0 or target_amount <= max_amount):
                score += 12
                evidence.append("가입금액 조건 충족")
            elif min_amount > 0 and target_amount < min_amount:
                score -= 6

        if period_months > 0:
            if min_term <= period_months and (max_term == 0 or period_months <= max_term):
                score += 10
                evidence.append("가입기간 조건 충족")

        if "HIGH_RATE" in interest_conditions and max_rate >= 4.0:
            score += 10
            evidence.append("고금리 관심 조건 반영")

        if "MOBILE" in interest_conditions and self.get_text(product, "mobile_join_yn", "mobileJoinYn").upper() == "Y":
            score += 8
            evidence.append("모바일 선호 조건 반영")

        if "PROTECTION" in interest_conditions and self.get_text(product, "depositor_protection_yn", "depositorProtectionYn").upper() == "Y":
            score += 7
            evidence.append("예금자보호 대상")

        if "LOW_AMOUNT" in interest_conditions and min_amount <= 10000:
            score += 7
            evidence.append("소액 가입 가능")

        score = max(0, min(score, 100))
        fit_percent = max(60, min(score + 8, 98))
        benefit_chance_percent = max(50, min(60 + len(evidence) * 5, 95))

        if not evidence:
            evidence.append("기본 조건 기준 추천 후보")

        return {
            "score": score,
            "fitPercent": fit_percent,
            "benefitChancePercent": benefit_chance_percent,
            "evidence": evidence[:5]
        }

    # ============================================================
    # OpenAI 호출
    # ============================================================

    def _generate(self, prompt: str) -> Optional[str]:
        if not self._has_valid_api_key():
            return None

        request_body = {
            "model": self.model,
            "input": prompt,
            "max_output_tokens": 700
        }

        request = urllib.request.Request(
            "https://api.openai.com/v1/responses",
            data=json.dumps(request_body).encode("utf-8"),
            headers={
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json"
            },
            method="POST"
        )

        try:
            with urllib.request.urlopen(request, timeout=self.timeout_seconds) as response:
                raw = response.read().decode("utf-8")
                data = json.loads(raw)
                return self._extract_output_text(data)

        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, Exception):
            return None

    def _extract_output_text(self, data: Dict[str, Any]) -> Optional[str]:
        output_text = data.get("output_text")

        if isinstance(output_text, str) and output_text.strip():
            return output_text.strip()

        output = data.get("output")

        if isinstance(output, list):
            texts = []

            for item in output:
                content = item.get("content") if isinstance(item, dict) else None

                if isinstance(content, list):
                    for content_item in content:
                        if not isinstance(content_item, dict):
                            continue

                        text = content_item.get("text")

                        if isinstance(text, str) and text.strip():
                            texts.append(text.strip())

            if texts:
                return "\n".join(texts).strip()

        return None

    def _has_valid_api_key(self) -> bool:
        return bool(self.api_key and self.api_key.strip() and "*" not in self.api_key)

    # ============================================================
    # dict helper
    # ============================================================

    def get_text(self, data: Dict[str, Any], *keys: str, default: str = "") -> str:
        value = self._get_value(data, *keys)

        if value is None:
            return default

        return str(value).strip()

    def get_int(self, data: Dict[str, Any], *keys: str, default: int = 0) -> int:
        value = self._get_value(data, *keys)

        if value is None or value == "":
            return default

        try:
            return int(float(value))
        except (TypeError, ValueError):
            return default

    def get_float(self, data: Dict[str, Any], *keys: str, default: float = 0.0) -> float:
        value = self._get_value(data, *keys)

        if value is None or value == "":
            return default

        try:
            return float(value)
        except (TypeError, ValueError):
            return default

    def _get_value(self, data: Dict[str, Any], *keys: str) -> Any:
        if data is None:
            return None

        for key in keys:
            if key in data:
                return data.get(key)

        return None

    def _product_type_label(self, product: Dict[str, Any]) -> str:
        product_type = self.get_text(product, "product_type", "productType").upper()

        if product_type == "DEPOSIT":
            return "예금"
        if product_type == "SAVINGS":
            return "적금"

        return "금융"

    def _join_method_text(self, product: Dict[str, Any]) -> str:
        methods = []

        if self.get_text(product, "branch_join_yn", "branchJoinYn").upper() == "Y":
            methods.append("영업점")
        if self.get_text(product, "internet_join_yn", "internetJoinYn").upper() == "Y":
            methods.append("인터넷뱅킹")
        if self.get_text(product, "mobile_join_yn", "mobileJoinYn").upper() == "Y":
            methods.append("모바일뱅킹")

        return ", ".join(methods)

    def _format_money(self, amount: int) -> str:
        if amount <= 0:
            return "제한 없음"

        return f"{amount:,}원"

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