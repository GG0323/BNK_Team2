package com.example.bnk.service.product.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendItemDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendRequestDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendResponseDto;
import com.example.bnk.service.product.ProductViewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductPersonaRecommendService {

    private final ProductViewService productViewService;
    private final ProductRecommendAiService productRecommendAiService;

    public ProductPersonaRecommendResponseDto recommend(ProductPersonaRecommendRequestDto request) {
        if (request == null) {
            request = new ProductPersonaRecommendRequestDto();
        }

        List<ProductListViewDto> productList = productViewService.getProductList("maxRateDesc", "ALL");

        List<ScoredProduct> scoredProducts = new ArrayList<>();

        for (ProductListViewDto product : productList) {
            ProductDetailViewDto detail = productViewService.getProductDetail(product.getProduct_no());

            if (detail == null) {
                continue;
            }

            if (!isAgeAvailable(detail, request.getAge())) {
                continue;
            }

            ScoredProduct scoredProduct = scoreProduct(detail, request);
            scoredProducts.add(scoredProduct);
        }

        scoredProducts.sort(Comparator.comparingInt(ScoredProduct::getRawScore).reversed());

        normalizeRankScores(scoredProducts);

        List<ProductPersonaRecommendItemDto> resultItems = new ArrayList<>();

        int limit = Math.min(3, scoredProducts.size());

        for (int i = 0; i < limit; i++) {
            ScoredProduct scoredProduct = scoredProducts.get(i);
            ProductDetailViewDto product = scoredProduct.getProduct();

            ProductPersonaRecommendItemDto item = new ProductPersonaRecommendItemDto();

            item.setProductNo(product.getProduct_no());
            item.setProductName(product.getProduct_name());
            item.setProductType(product.getProduct_type());
            item.setSubtitle(product.getSubtitle());

            item.setMinInterestRate(product.getMin_interest_rate());
            item.setMaxInterestRate(product.getMax_interest_rate());

            item.setMinJoinAmount(product.getMin_join_amount());
            item.setMaxJoinAmount(product.getMax_join_amount());

            item.setBranchJoinYn(product.getBranch_join_yn());
            item.setInternetJoinYn(product.getInternet_join_yn());
            item.setMobileJoinYn(product.getMobile_join_yn());

            item.setScore(scoredProduct.getFitPercent());
            item.setFitPercent(scoredProduct.getFitPercent());
            item.setBenefitChancePercent(scoredProduct.getBenefitChancePercent());

            item.setEvidence(scoredProduct.getEvidence());
            item.setDetailUrl("/products/detail?product_no=" + product.getProduct_no());

            String reason = productRecommendAiService.createPersonalRecommendReason(
                    product,
                    request,
                    scoredProduct.getFitPercent(),
                    scoredProduct.getBenefitChancePercent(),
                    scoredProduct.getEvidence()
            );

            item.setReason(reason);

            resultItems.add(item);
        }

        ProductPersonaRecommendResponseDto response = new ProductPersonaRecommendResponseDto();
        response.setRecommendedProducts(resultItems);
        response.setSummary(createSummary(request, resultItems));

        return response;
    }

    private boolean isAgeAvailable(ProductDetailViewDto product, int age) {
        if (age <= 0) {
            return true;
        }

        if (product.getMin_age() > 0 && age < product.getMin_age()) {
            return false;
        }

        return product.getMax_age() <= 0 || age <= product.getMax_age();
    }

    private ScoredProduct scoreProduct(ProductDetailViewDto product, ProductPersonaRecommendRequestDto request) {
        int rawScore = 45;
        int benefitChance = 45;

        Set<String> evidenceSet = new LinkedHashSet<>();

        String purpose = request.getPurposeSafe();
        String preferredProductType = request.getPreferredProductTypeSafe();
        String preferredChannel = request.getPreferredChannelSafe();
        List<String> interestConditions = request.getInterestConditionsSafe();

        String productType = product.getProduct_type();

        /*
         * 1. 선호 상품 유형
         */
        if ("ALL".equals(preferredProductType)) {
            rawScore += 4;
        } else if (preferredProductType.equals(productType)) {
            rawScore += 16;
            evidenceSet.add("선호 상품 유형 일치");
        } else {
            rawScore -= 18;
            evidenceSet.add("선호 상품 유형과 다름");
        }

        /*
         * 2. 가입 목적
         */
        if ("MAKE_MONEY".equals(purpose)) {
            if ("SAVINGS".equals(productType)) {
                rawScore += 18;
                evidenceSet.add("목돈 만들기 목적에 적합");
            } else {
                rawScore -= 8;
            }
        }

        if ("ROLL_MONEY".equals(purpose)) {
            if ("DEPOSIT".equals(productType)) {
                rawScore += 18;
                evidenceSet.add("목돈 굴리기 목적에 적합");
            } else {
                rawScore -= 8;
            }
        }

        if ("HIGH_RATE".equals(purpose)) {
            int rateScore = calculateRateScore(product.getMax_interest_rate(), 6);
            rawScore += rateScore;
            evidenceSet.add("고금리 우선 조건 반영");
        }

        if ("EMERGENCY".equals(purpose)) {
            if (product.getMin_join_amount() <= 10000) {
                rawScore += 14;
                evidenceSet.add("비상금 목적에 맞는 소액 시작 가능");
            } else {
                rawScore -= 6;
            }
        }

        /*
         * 3. 금리 점수
         */
        int baseRateScore = calculateRateScore(product.getMax_interest_rate(), 4);
        rawScore += baseRateScore;
        evidenceSet.add("최고금리 연 " + formatRate(product.getMax_interest_rate()) + "%");

        /*
         * 4. 금액 조건
         */
        long availableAmount = getAvailableAmountForProduct(product, request);

        if (availableAmount > 0) {
            if (product.getMin_join_amount() <= availableAmount) {
                rawScore += 12;
                evidenceSet.add("가입 가능 금액 조건 충족");
            } else {
                rawScore -= 22;
                evidenceSet.add("최소 가입금액 확인 필요");
            }
        }

        if (product.getMin_join_amount() <= 10000) {
            rawScore += 5;

            if (interestConditions.contains("LOW_AMOUNT")) {
                rawScore += 11;
                evidenceSet.add("소액 시작 가능");
            }
        } else if (product.getMin_join_amount() >= 1000000) {
            if ("MAKE_MONEY".equals(purpose) || "EMERGENCY".equals(purpose)) {
                rawScore -= 8;
            }
        }

        /*
         * 5. 가입 기간
         */
        if (request.getPeriodMonths() > 0) {
            if (isPeriodAvailable(product, request.getPeriodMonths())) {
                rawScore += 7;
                evidenceSet.add("희망 가입 기간 조건과 유사");
            } else {
                rawScore -= 9;
                evidenceSet.add("희망 가입 기간과 차이 있음");
            }
        }

        /*
         * 6. 가입 채널
         */
        if ("MOBILE".equals(preferredChannel)) {
            if ("Y".equals(product.getMobile_join_yn())) {
                rawScore += 14;
                benefitChance += 9;
                evidenceSet.add("모바일 가입 가능");
            } else {
                rawScore -= 12;
                evidenceSet.add("모바일 가입 불가");
            }
        }

        if ("INTERNET".equals(preferredChannel)) {
            if ("Y".equals(product.getInternet_join_yn())) {
                rawScore += 10;
                evidenceSet.add("인터넷 가입 가능");
            } else {
                rawScore -= 8;
            }
        }

        if ("BRANCH".equals(preferredChannel)) {
            if ("Y".equals(product.getBranch_join_yn())) {
                rawScore += 10;
                evidenceSet.add("영업점 가입 가능");
            } else {
                rawScore -= 8;
            }
        }

        if ("ALL".equals(preferredChannel) && "Y".equals(product.getMobile_join_yn())) {
            rawScore += 4;
            evidenceSet.add("모바일 가입 가능");
        }

        /*
         * 7. 관심 조건
         */
        if (interestConditions.contains("HIGH_RATE")) {
            rawScore += calculateRateScore(product.getMax_interest_rate(), 3);
        }

        if (interestConditions.contains("MOBILE")) {
            if ("Y".equals(product.getMobile_join_yn())) {
                rawScore += 8;
                benefitChance += 6;
                evidenceSet.add("모바일 선호 조건 반영");
            } else {
                rawScore -= 5;
            }
        }

        if (interestConditions.contains("PROTECTION")) {
            if ("Y".equals(product.getDepositor_protection_yn())) {
                rawScore += 6;
                evidenceSet.add("예금자보호 대상");
            } else {
                rawScore -= 4;
            }
        }

        if (interestConditions.contains("PREFERENTIAL_RATE")) {
            if (hasPreferentialCondition(product)) {
                rawScore += 9;
                benefitChance += 12;
                evidenceSet.add("우대조건 확인 가능");
            } else {
                rawScore -= 6;
                evidenceSet.add("우대조건 확인 필요");
            }
        }

        /*
         * 8. 우대조건 충족 가능성
         */
        benefitChance += calculateBenefitChanceBonus(product, request);
        benefitChance = clamp(benefitChance, 28, 95);

        /*
         * 9. 상품별 과도한 점수 방지
         */
        rawScore = clamp(rawScore, 1, 130);

        List<String> evidence = new ArrayList<>(evidenceSet);

        if (evidence.isEmpty()) {
            evidence.add("조건 기반 추천");
        }

        return new ScoredProduct(product, rawScore, benefitChance, evidence);
    }

    /*
     * rawScore가 다 높게 나와도 최종 적합도는 1/2/3위가 자연스럽게 갈리도록 보정한다.
     */
    private void normalizeRankScores(List<ScoredProduct> scoredProducts) {
        if (scoredProducts == null || scoredProducts.isEmpty()) {
            return;
        }

        int topRawScore = scoredProducts.get(0).getRawScore();

        for (int i = 0; i < scoredProducts.size(); i++) {
            ScoredProduct product = scoredProducts.get(i);

            int rawGap = Math.max(0, topRawScore - product.getRawScore());
            int rankPenalty = i * 4;

            int normalized = 92 - rawGap - rankPenalty;

            if (i == 0 && product.getRawScore() >= 95) {
                normalized += 3;
            }

            if (i == 0) {
                normalized = clamp(normalized, 88, 96);
            } else if (i == 1) {
                normalized = clamp(normalized, 80, 91);
            } else if (i == 2) {
                normalized = clamp(normalized, 72, 87);
            } else {
                normalized = clamp(normalized, 50, 82);
            }

            product.setFitPercent(normalized);
        }
    }

    private int calculateRateScore(double maxRate, int multiplier) {
        return Math.min((int) Math.round(maxRate * multiplier), 24);
    }

    private long getAvailableAmountForProduct(ProductDetailViewDto product, ProductPersonaRecommendRequestDto request) {
        if ("SAVINGS".equals(product.getProduct_type())) {
            return request.getMonthlyAmount() > 0 ? request.getMonthlyAmount() : request.getBalance();
        }

        return request.getBalance();
    }

    private boolean isPeriodAvailable(ProductDetailViewDto product, int periodMonths) {
        int minTerm = product.getMin_term_months();
        int maxTerm = product.getMax_term_months();

        if (minTerm <= 0 && maxTerm <= 0) {
            return true;
        }

        if (minTerm > 0 && periodMonths < minTerm) {
            return false;
        }

        return maxTerm <= 0 || periodMonths <= maxTerm;
    }

    private boolean hasPreferentialCondition(ProductDetailViewDto product) {
        String text = normalizeText(
                product.getPreferential_rate_summary()
                        + " "
                        + product.getCondition_note()
                        + " "
                        + product.getJoin_method_desc()
        );

        return text.contains("우대")
                || text.contains("급여")
                || text.contains("자동이체")
                || text.contains("카드")
                || text.contains("모바일")
                || text.contains("비대면");
    }

    private int calculateBenefitChanceBonus(ProductDetailViewDto product, ProductPersonaRecommendRequestDto request) {
        int bonus = 0;

        String text = normalizeText(
                product.getPreferential_rate_summary()
                        + " "
                        + product.getCondition_note()
                        + " "
                        + product.getJoin_method_desc()
        );

        List<String> conditions = request.getInterestConditionsSafe();

        if (text.contains("모바일") || text.contains("비대면")) {
            bonus += 8;
        }

        if (text.contains("자동이체")) {
            bonus += 6;
        }

        if (text.contains("급여")) {
            bonus += 4;
        }

        if (text.contains("카드")) {
            bonus += 4;
        }

        if (conditions.contains("MOBILE") && "Y".equals(product.getMobile_join_yn())) {
            bonus += 8;
        }

        if (conditions.contains("LOW_AMOUNT") && product.getMin_join_amount() <= 10000) {
            bonus += 8;
        }

        if (conditions.contains("HIGH_RATE") && product.getMax_interest_rate() >= 3.0) {
            bonus += 6;
        }

        if (conditions.contains("PREFERENTIAL_RATE") && hasPreferentialCondition(product)) {
            bonus += 8;
        }

        return bonus;
    }

    private String createSummary(ProductPersonaRecommendRequestDto request,
                                 List<ProductPersonaRecommendItemDto> items) {

        if (items == null || items.isEmpty()) {
            return "입력한 조건에 맞는 추천 상품을 찾지 못했습니다. 금액이나 상품 유형 조건을 조금 완화해 다시 시도해 주세요.";
        }

        String purposeText = getPurposeText(request.getPurposeSafe());
        ProductPersonaRecommendItemDto topItem = items.get(0);

        return purposeText
                + " 기준으로 "
                + topItem.getProductName()
                + "을/를 가장 우선 추천합니다. "
                + "적합도는 "
                + topItem.getFitPercent()
                + "%, 우대조건 충족 가능성은 "
                + topItem.getBenefitChancePercent()
                + "%로 계산되었습니다. "
                + "금리·가입금액·가입채널·관심 조건을 함께 반영했습니다.";
    }

    private String getPurposeText(String purpose) {
        if ("MAKE_MONEY".equals(purpose)) {
            return "목돈 만들기 목적";
        }

        if ("ROLL_MONEY".equals(purpose)) {
            return "목돈 굴리기 목적";
        }

        if ("HIGH_RATE".equals(purpose)) {
            return "고금리 우선 조건";
        }

        if ("EMERGENCY".equals(purpose)) {
            return "비상금 마련 목적";
        }

        return "입력한 조건";
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("null", "").trim();
    }

    private String formatRate(double rate) {
        return String.format("%.2f", rate);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class ScoredProduct {
        private final ProductDetailViewDto product;
        private final int rawScore;
        private final int benefitChancePercent;
        private final List<String> evidence;

        private int fitPercent;

        private ScoredProduct(ProductDetailViewDto product,
                              int rawScore,
                              int benefitChancePercent,
                              List<String> evidence) {
            this.product = product;
            this.rawScore = rawScore;
            this.benefitChancePercent = benefitChancePercent;
            this.evidence = evidence;
            this.fitPercent = rawScore;
        }

        public ProductDetailViewDto getProduct() {
            return product;
        }

        public int getRawScore() {
            return rawScore;
        }

        public int getBenefitChancePercent() {
            return benefitChancePercent;
        }

        public List<String> getEvidence() {
            return evidence;
        }

        public int getFitPercent() {
            return fitPercent;
        }

        public void setFitPercent(int fitPercent) {
            this.fitPercent = fitPercent;
        }
    }
}