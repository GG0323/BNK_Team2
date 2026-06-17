package com.example.bnk.service.product.ai;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductRecommendAiService {

    private final ProductAiPromptBuilder productAiPromptBuilder;
    private final ProductGptClient productGptClient;

    /**
     * 기존 추천 이유 생성
     */
    public String createRecommendReason(ProductListViewDto product) {
        if (product == null) {
            return "추천할 상품 정보가 없습니다.";
        }

        String prompt = productAiPromptBuilder.buildRecommendPrompt(product);
        String aiReason = productGptClient.generate(prompt);

        if (aiReason == null || aiReason.trim().isEmpty()) {
            return createFallbackRecommendReason(product);
        }

        return aiReason;
    }

    /**
     * 사용자 페르소나 기반 추천 이유 생성
     */
    public String createPersonalRecommendReason(ProductDetailViewDto product,
                                                ProductPersonaRecommendRequestDto request,
                                                int score,
                                                int benefitChancePercent,
                                                List<String> evidence) {

        if (product == null) {
            return "추천할 상품 정보가 없습니다.";
        }

        String prompt = buildPersonalRecommendPrompt(
                product,
                request,
                score,
                benefitChancePercent,
                evidence
        );

        String aiReason = productGptClient.generate(prompt);

        if (aiReason == null || aiReason.trim().isEmpty()) {
            return createFallbackPersonalRecommendReason(
                    product,
                    request,
                    score,
                    benefitChancePercent
            );
        }

        return aiReason;
    }

    /**
     * 기존 fallback 추천 이유
     */
    public String createFallbackRecommendReason(ProductListViewDto product) {
        String productType = "DEPOSIT".equals(product.getProduct_type()) ? "예금" : "적금";

        String mobileText = "Y".equals(product.getMobile_join_yn())
                ? "모바일 가입이 가능해 접근성이 좋습니다."
                : "영업점 또는 다른 채널을 통해 가입 가능 여부를 확인해야 합니다.";

        return product.getProduct_name()
                + "은/는 "
                + productType
                + " 상품이며, 최고 금리 "
                + product.getMax_interest_rate()
                + "%를 기준으로 비교해볼 만한 상품입니다. "
                + mobileText;
    }

    private String buildPersonalRecommendPrompt(ProductDetailViewDto product,
                                                ProductPersonaRecommendRequestDto request,
                                                int score,
                                                int benefitChancePercent,
                                                List<String> evidence) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("너는 BNK 부산은행 예금/적금 상품 추천을 도와주는 금융 상담 AI야.\n");
        prompt.append("아래 사용자 조건과 상품 정보를 바탕으로 고객에게 보여줄 개인화 추천 이유를 작성해줘.\n\n");

        prompt.append("[작성 조건]\n");
        prompt.append("- 한국어로 작성\n");
        prompt.append("- 3~5문장 정도\n");
        prompt.append("- 확정적인 투자 조언처럼 말하지 말고 참고용 안내처럼 작성\n");
        prompt.append("- 상품의 장점과 확인해야 할 점을 함께 안내\n");
        prompt.append("- 금리, 가입금액, 가입채널, 우대조건 충족 가능성을 자연스럽게 포함\n\n");

        prompt.append("[사용자 조건]\n");
        prompt.append("나이: ").append(request.getAge()).append("\n");
        prompt.append("현재 사용 가능 금액: ").append(request.getBalance()).append("\n");
        prompt.append("월 납입 가능 금액: ").append(request.getMonthlyAmount()).append("\n");
        prompt.append("희망 가입 기간: ").append(request.getPeriodMonths()).append("\n");
        prompt.append("가입 목적: ").append(request.getPurposeSafe()).append("\n");
        prompt.append("선호 상품 유형: ").append(request.getPreferredProductTypeSafe()).append("\n");
        prompt.append("선호 가입 채널: ").append(request.getPreferredChannelSafe()).append("\n");
        prompt.append("관심 조건: ").append(request.getInterestConditionsSafe()).append("\n\n");

        prompt.append("[추천 상품 정보]\n");
        prompt.append("상품명: ").append(product.getProduct_name()).append("\n");
        prompt.append("상품 유형: ").append(product.getProduct_type()).append("\n");
        prompt.append("최저금리: ").append(product.getMin_interest_rate()).append("\n");
        prompt.append("최고금리: ").append(product.getMax_interest_rate()).append("\n");
        prompt.append("최소 가입금액: ").append(product.getMin_join_amount()).append("\n");
        prompt.append("최대 가입금액: ").append(product.getMax_join_amount()).append("\n");
        prompt.append("가입 기간: ").append(product.getMin_term_months()).append("개월 ~ ")
                .append(product.getMax_term_months()).append("개월\n");
        prompt.append("영업점 가입 가능 여부: ").append(product.getBranch_join_yn()).append("\n");
        prompt.append("인터넷 가입 가능 여부: ").append(product.getInternet_join_yn()).append("\n");
        prompt.append("모바일 가입 가능 여부: ").append(product.getMobile_join_yn()).append("\n");
        prompt.append("우대금리 요약: ").append(product.getPreferential_rate_summary()).append("\n");
        prompt.append("우대조건: ").append(product.getCondition_note()).append("\n");
        prompt.append("예금자보호 여부: ").append(product.getDepositor_protection_yn()).append("\n\n");

        prompt.append("[추천 계산 결과]\n");
        prompt.append("적합도: ").append(score).append("%\n");
        prompt.append("우대조건 충족 가능성: ").append(benefitChancePercent).append("%\n");
        prompt.append("추천 근거: ").append(evidence).append("\n");

        return prompt.toString();
    }

    private String createFallbackPersonalRecommendReason(ProductDetailViewDto product,
                                                         ProductPersonaRecommendRequestDto request,
                                                         int score,
                                                         int benefitChancePercent) {

        String productType = "DEPOSIT".equals(product.getProduct_type()) ? "예금" : "적금";

        StringBuilder reason = new StringBuilder();

        reason.append(product.getProduct_name())
                .append("은/는 ")
                .append(productType)
                .append(" 상품이며, 입력한 조건 기준 적합도 ")
                .append(score)
                .append("%로 추천할 수 있습니다. ");

        if ("MAKE_MONEY".equals(request.getPurposeSafe()) && "SAVINGS".equals(product.getProduct_type())) {
            reason.append("목돈 만들기 목적과 잘 맞는 적금 상품으로 볼 수 있습니다. ");
        } else if ("ROLL_MONEY".equals(request.getPurposeSafe()) && "DEPOSIT".equals(product.getProduct_type())) {
            reason.append("목돈을 일정 기간 운용하려는 목적과 잘 맞는 예금 상품으로 볼 수 있습니다. ");
        } else if ("HIGH_RATE".equals(request.getPurposeSafe())) {
            reason.append("최고금리 연 ")
                    .append(product.getMax_interest_rate())
                    .append("%를 기준으로 금리 조건을 비교해볼 만합니다. ");
        } else {
            reason.append("금리와 가입 조건을 함께 비교해볼 만한 상품입니다. ");
        }

        if ("Y".equals(product.getMobile_join_yn())) {
            reason.append("모바일 가입이 가능해 비대면 가입을 선호하는 고객에게 접근성이 좋습니다. ");
        } else if ("Y".equals(product.getBranch_join_yn())) {
            reason.append("영업점 가입이 가능해 상담을 받고 가입하려는 고객에게 적합합니다. ");
        }

        reason.append("우대조건 충족 가능성은 ")
                .append(benefitChancePercent)
                .append("% 수준으로 계산되었으며, 가입 전 세부 우대조건과 상품설명서를 확인하는 것이 좋습니다.");

        return reason.toString();
    }
}