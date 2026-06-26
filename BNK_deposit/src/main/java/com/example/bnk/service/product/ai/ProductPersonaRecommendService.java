package com.example.bnk.service.product.ai;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        if (productList != null) {
            for (ProductListViewDto product : productList) {
                if (product == null) {
                    continue;
                }

                ProductDetailViewDto detail = productViewService.getProductDetail(product.getProduct_no());

                if (detail == null) {
                    continue;
                }

                ScoredProduct scoredProduct = scoreProduct(detail, request);
                scoredProducts.add(scoredProduct);
            }
        }

        scoredProducts.sort(Comparator.comparingInt(ScoredProduct::getRawScore).reversed());
        normalizeRankScores(scoredProducts);

        List<ProductPersonaRecommendItemDto> resultItems = new ArrayList<>();

        int limit = Math.min(3, scoredProducts.size());

        for (int i = 0; i < limit; i++) {
            ScoredProduct scoredProduct = scoredProducts.get(i);
            ProductDetailViewDto product = scoredProduct.getProduct();

            ProductPersonaRecommendItemDto item = new ProductPersonaRecommendItemDto();

            item.setProductNo(toLong(readValue(product, "product_no", "productNo")));
            item.setProductName(toText(readValue(product, "product_name", "productName")));
            item.setProductType(toText(readValue(product, "product_type", "productType")));
            item.setSubtitle(toText(readValue(product, "subtitle")));

            item.setMinInterestRate(toDouble(readValue(product, "min_interest_rate", "minInterestRate")));
            item.setMaxInterestRate(toDouble(readValue(product, "max_interest_rate", "maxInterestRate")));

            item.setMinJoinAmount(toLong(readValue(product, "min_join_amount", "minJoinAmount")));
            item.setMaxJoinAmount(toLong(readValue(product, "max_join_amount", "maxJoinAmount")));

            item.setBranchJoinYn(toText(readValue(product, "branch_join_yn", "branchJoinYn")));
            item.setInternetJoinYn(toText(readValue(product, "internet_join_yn", "internetJoinYn")));
            item.setMobileJoinYn(toText(readValue(product, "mobile_join_yn", "mobileJoinYn")));

            item.setScore(scoredProduct.getScore());
            item.setFitPercent(scoredProduct.getFitPercent());
            item.setBenefitChancePercent(scoredProduct.getBenefitChancePercent());

            item.setEvidence(scoredProduct.getEvidence());
            item.setDetailUrl("/products/detail?product_no=" + item.getProductNo());

            String reason = createReason(product, request, scoredProduct);

            item.setReason(reason);

            resultItems.add(item);
        }

        ProductPersonaRecommendResponseDto response = new ProductPersonaRecommendResponseDto();

        response.setSummary(createSummary(request, resultItems));
        response.setRecommendedProducts(resultItems);

        return response;
    }

    private ScoredProduct scoreProduct(ProductDetailViewDto product, ProductPersonaRecommendRequestDto request) {
        int score = 0;
        List<String> evidence = new ArrayList<>();

        String productType = toText(readValue(product, "product_type", "productType")).toUpperCase();
        String preferredProductType = toText(readRequestValue(request, "preferredProductTypeSafe", "preferredProductType"), "ALL").toUpperCase();
        String preferredChannel = toText(readRequestValue(request, "preferredChannelSafe", "preferredChannel"), "ALL").toUpperCase();
        String purpose = toText(readRequestValue(request, "purposeSafe", "purpose"), "MAKE_MONEY").toUpperCase();

        double maxRate = toDouble(readValue(product, "max_interest_rate", "maxInterestRate"));
        long minAmount = toLong(readValue(product, "min_join_amount", "minJoinAmount"));
        long maxAmount = toLong(readValue(product, "max_join_amount", "maxJoinAmount"));
        int minTerm = toInt(readValue(product, "min_term_months", "minTermMonths"));
        int maxTerm = toInt(readValue(product, "max_term_months", "maxTermMonths"));

        long balance = toLong(readRequestValue(request, "balance"));
        long monthlyAmount = toLong(readRequestValue(request, "monthlyAmount"));
        int periodMonths = toInt(readRequestValue(request, "periodMonths"));

        if ("ALL".equals(preferredProductType) || preferredProductType.equals(productType)) {
            score += 18;
            evidence.add("선호 상품 유형 일치");
        }

        if (maxRate >= 4.0) {
            score += 22;
            evidence.add("최고금리 연 " + maxRate + "%");
        } else if (maxRate >= 3.0) {
            score += 16;
            evidence.add("금리 조건 양호");
        } else {
            score += 8;
        }

        if ("MAKE_MONEY".equals(purpose) && "SAVINGS".equals(productType)) {
            score += 14;
            evidence.add("목돈 만들기 목적에 적금 유형 적합");
        } else if ("ROLL_MONEY".equals(purpose) && "DEPOSIT".equals(productType)) {
            score += 14;
            evidence.add("목돈 굴리기 목적에 예금 유형 적합");
        } else if ("HIGH_RATE".equals(purpose)) {
            score += 10;
            evidence.add("고금리 우선 조건 반영");
        }

        boolean mobileJoin = "Y".equalsIgnoreCase(toText(readValue(product, "mobile_join_yn", "mobileJoinYn")));
        boolean internetJoin = "Y".equalsIgnoreCase(toText(readValue(product, "internet_join_yn", "internetJoinYn")));
        boolean branchJoin = "Y".equalsIgnoreCase(toText(readValue(product, "branch_join_yn", "branchJoinYn")));

        if ("MOBILE".equals(preferredChannel) && mobileJoin) {
            score += 13;
            evidence.add("모바일 가입 가능");
        } else if ("INTERNET".equals(preferredChannel) && internetJoin) {
            score += 13;
            evidence.add("인터넷 가입 가능");
        } else if ("BRANCH".equals(preferredChannel) && branchJoin) {
            score += 13;
            evidence.add("영업점 가입 가능");
        } else if ("ALL".equals(preferredChannel)) {
            score += 8;
        }

        long targetAmount = "SAVINGS".equals(productType) ? monthlyAmount : balance;

        if (targetAmount > 0) {
            if (minAmount <= targetAmount && (maxAmount <= 0 || targetAmount <= maxAmount)) {
                score += 12;
                evidence.add("가입금액 조건 충족");
            } else if (minAmount > targetAmount) {
                score -= 6;
            }
        }

        if (periodMonths > 0) {
            if (minTerm <= periodMonths && (maxTerm <= 0 || periodMonths <= maxTerm)) {
                score += 10;
                evidence.add("가입기간 조건 충족");
            }
        }

        List<String> interestConditions = getStringList(
                readRequestValue(request, "interestConditionsSafe", "interestConditions")
        );

        if (interestConditions.contains("HIGH_RATE") && maxRate >= 4.0) {
            score += 10;
            evidence.add("고금리 관심 조건 반영");
        }

        if (interestConditions.contains("MOBILE") && mobileJoin) {
            score += 8;
            evidence.add("모바일 선호 조건 반영");
        }

        if (interestConditions.contains("PROTECTION")
                && "Y".equalsIgnoreCase(toText(readValue(product, "depositor_protection_yn", "depositorProtectionYn")))) {
            score += 7;
            evidence.add("예금자보호 대상");
        }

        if (interestConditions.contains("LOW_AMOUNT") && minAmount <= 10000) {
            score += 7;
            evidence.add("소액 가입 가능");
        }

        if (evidence.isEmpty()) {
            evidence.add("기본 조건 기준 추천 후보");
        }

        int rawScore = Math.max(0, Math.min(score, 100));
        int fitPercent = Math.max(60, Math.min(rawScore + 8, 98));
        int benefitChancePercent = Math.max(50, Math.min(60 + evidence.size() * 5, 95));

        return new ScoredProduct(
                product,
                rawScore,
                fitPercent,
                benefitChancePercent,
                evidence
        );
    }

    private void normalizeRankScores(List<ScoredProduct> scoredProducts) {
        if (scoredProducts == null || scoredProducts.isEmpty()) {
            return;
        }

        int rank = 0;

        for (ScoredProduct scoredProduct : scoredProducts) {
            int adjusted = Math.max(60, Math.min(98, scoredProduct.getFitPercent() - rank * 3));
            scoredProduct.setFitPercent(adjusted);
            rank++;
        }
    }

    private String createReason(
            ProductDetailViewDto product,
            ProductPersonaRecommendRequestDto request,
            ScoredProduct scoredProduct
    ) {
        try {
            String aiReason = productRecommendAiService.createPersonalRecommendReason(
                    product,
                    request,
                    scoredProduct.getFitPercent(),
                    scoredProduct.getBenefitChancePercent(),
                    scoredProduct.getEvidence()
            );

            if (aiReason != null && !aiReason.trim().isEmpty()) {
                return aiReason.trim();
            }

        } catch (Exception e) {
            System.out.println("[상품 AI] Spring 추천 이유 생성 실패. fallback 사용. message=" + e.getMessage());
        }

        return createFallbackReason(product, scoredProduct);
    }

    private String createFallbackReason(ProductDetailViewDto product, ScoredProduct scoredProduct) {
        String productName = toText(readValue(product, "product_name", "productName"), "해당 상품");
        String productType = toText(readValue(product, "product_type", "productType")).toUpperCase();
        double maxRate = toDouble(readValue(product, "max_interest_rate", "maxInterestRate"));

        String productTypeLabel = "DEPOSIT".equals(productType) ? "예금" : "적금";

        StringBuilder reason = new StringBuilder();

        reason.append(productName)
                .append("은/는 ")
                .append(productTypeLabel)
                .append(" 상품이며, 입력하신 조건 기준 적합도 ")
                .append(scoredProduct.getFitPercent())
                .append("%로 추천할 수 있습니다. ");

        reason.append("최고금리 연 ")
                .append(maxRate)
                .append("%를 기준으로 금리 조건을 비교해볼 만합니다. ");

        if (scoredProduct.getEvidence() != null && !scoredProduct.getEvidence().isEmpty()) {
            reason.append("추천 근거로는 ")
                    .append(String.join(", ", scoredProduct.getEvidence()))
                    .append(" 조건이 반영되었습니다. ");
        }

        reason.append("가입 전 세부 우대조건과 상품설명서를 함께 확인하는 것이 좋습니다.");

        return reason.toString();
    }

    private String createSummary(
            ProductPersonaRecommendRequestDto request,
            List<ProductPersonaRecommendItemDto> resultItems
    ) {
        if (resultItems == null || resultItems.isEmpty()) {
            return "조건에 맞는 추천 상품을 찾지 못했습니다.";
        }

        ProductPersonaRecommendItemDto first = resultItems.get(0);

        String purpose = toText(readRequestValue(request, "purposeSafe", "purpose"), "MAKE_MONEY").toUpperCase();

        String purposeText;

        if ("ROLL_MONEY".equals(purpose)) {
            purposeText = "목돈 굴리기";
        } else if ("HIGH_RATE".equals(purpose)) {
            purposeText = "고금리 우선";
        } else if ("EMERGENCY".equals(purpose)) {
            purposeText = "비상금 관리";
        } else {
            purposeText = "목돈 만들기";
        }

        return purposeText
                + " 목적 기준으로 "
                + first.getProductName()
                + "을/를 가장 우선 추천합니다. 적합도는 "
                + first.getFitPercent()
                + "%, 우대조건 충족 가능성은 "
                + first.getBenefitChancePercent()
                + "%로 계산되었습니다. 금리, 가입금액, 가입채널, 관심 조건을 함께 반영했습니다.";
    }

    private Object readRequestValue(ProductPersonaRecommendRequestDto request, String... names) {
        if (request == null) {
            return null;
        }

        return readValue(request, names);
    }

    private Object readValue(Object source, String... names) {
        if (source == null || names == null) {
            return null;
        }

        for (String name : names) {
            Object value = readByMethod(source, name);

            if (value != null) {
                return value;
            }

            value = readByField(source, name);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private Object readByMethod(Object source, String name) {
        List<String> methodNames = new ArrayList<>();

        methodNames.add(name);
        methodNames.add("get" + capitalize(name));
        methodNames.add("is" + capitalize(name));

        String camelName = toCamelCase(name);

        methodNames.add("get" + capitalize(camelName));
        methodNames.add("is" + capitalize(camelName));

        for (String methodName : methodNames) {
            try {
                Method method = source.getClass().getMethod(methodName);

                if (method.getParameterCount() == 0) {
                    return method.invoke(source);
                }

            } catch (Exception ignored) {
                // 다음 getter 후보 확인
            }
        }

        return null;
    }

    private Object readByField(Object source, String name) {
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add(name);
        fieldNames.add(toCamelCase(name));

        Class<?> currentClass = source.getClass();

        while (currentClass != null && currentClass != Object.class) {
            for (String fieldName : fieldNames) {
                try {
                    Field field = currentClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(source);

                } catch (Exception ignored) {
                    // 다음 field 후보 확인
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    private String toText(Object value) {
        return toText(value, "");
    }

    private String toText(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return defaultValue;
        }

        return text;
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }

        try {
            return (int) Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        try {
            return (long) Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private List<String> getStringList(Object value) {
        List<String> result = new ArrayList<>();

        if (value == null) {
            return result;
        }

        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item != null) {
                    result.add(String.valueOf(item).trim().toUpperCase());
                }
            }

            return result;
        }

        if (value instanceof String) {
            String text = ((String) value).trim();

            if (text.isEmpty()) {
                return result;
            }

            String[] parts = text.split(",");

            for (String part : parts) {
                if (part != null && !part.trim().isEmpty()) {
                    result.add(part.trim().toUpperCase());
                }
            }

            return result;
        }

        return result;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String toCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;

        for (char ch : text.toCharArray()) {
            if (ch == '_') {
                upperNext = true;
                continue;
            }

            if (upperNext) {
                builder.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                builder.append(ch);
            }
        }

        return builder.toString();
    }

    private static class ScoredProduct {

        private final ProductDetailViewDto product;
        private final int rawScore;
        private final int score;
        private int fitPercent;
        private final int benefitChancePercent;
        private final List<String> evidence;

        private ScoredProduct(
                ProductDetailViewDto product,
                int rawScore,
                int fitPercent,
                int benefitChancePercent,
                List<String> evidence
        ) {
            this.product = product;
            this.rawScore = rawScore;
            this.score = rawScore;
            this.fitPercent = fitPercent;
            this.benefitChancePercent = benefitChancePercent;
            this.evidence = evidence == null ? Collections.emptyList() : evidence;
        }

        public ProductDetailViewDto getProduct() {
            return product;
        }

        public int getRawScore() {
            return rawScore;
        }

        public int getScore() {
            return score;
        }

        public int getFitPercent() {
            return fitPercent;
        }

        public void setFitPercent(int fitPercent) {
            this.fitPercent = fitPercent;
        }

        public int getBenefitChancePercent() {
            return benefitChancePercent;
        }

        public List<String> getEvidence() {
            return evidence;
        }
    }
}