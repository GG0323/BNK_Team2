package com.example.bnk.service.product.ai;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductCompareViewDto;

@Service
public class ProductCompareAiService {

    private final ProductAiPromptBuilder productAiPromptBuilder;
    private final ProductGptClient productGptClient;
    private final ProductFastApiClient productFastApiClient;

    public ProductCompareAiService(
            ProductAiPromptBuilder productAiPromptBuilder,
            ProductGptClient productGptClient,
            ProductFastApiClient productFastApiClient
    ) {
        this.productAiPromptBuilder = productAiPromptBuilder;
        this.productGptClient = productGptClient;
        this.productFastApiClient = productFastApiClient;
    }

    /**
     * 비교 상품 전체 AI 요약
     */
    public String createCompareSummary(List<ProductCompareViewDto> products) {
        if (products == null || products.isEmpty()) {
            return "비교할 상품 정보가 없습니다.";
        }

        String fastApiSummary = productFastApiClient.createCompareSummary(products);

        if (!isAiUnavailable(fastApiSummary)) {
            System.out.println("[상품 AI] FastAPI 비교 요약 결과 사용");
            return fastApiSummary;
        }

        String prompt = productAiPromptBuilder.buildComparePrompt(products);
        String aiSummary = productGptClient.generate(prompt);

        if (isAiUnavailable(aiSummary)) {
            return createFallbackCompareSummary(products);
        }

        return aiSummary;
    }

    /**
     * 비교 상품 개별 AI 요약
     */
    public String createCompareProductSummary(ProductCompareViewDto product) {
        if (product == null) {
            return "요약할 상품 정보가 없습니다.";
        }

        String fastApiSummary = productFastApiClient.createProductSummary(product);

        if (!isAiUnavailable(fastApiSummary)) {
            System.out.println("[상품 AI] FastAPI 개별 상품 요약 결과 사용");
            return fastApiSummary;
        }

        String prompt = productAiPromptBuilder.buildCompareProductSummaryPrompt(product);
        String aiSummary = productGptClient.generate(prompt);

        if (isAiUnavailable(aiSummary)) {
            return createFallbackCompareProductSummary(product);
        }

        return aiSummary;
    }

    /**
     * 전체 비교 요약 fallback
     */
    public String createFallbackCompareSummary(List<ProductCompareViewDto> products) {
        if (products == null || products.isEmpty()) {
            return "비교할 상품 정보가 없습니다.";
        }

        ProductCompareViewDto highestMaxRateProduct = products.get(0);
        ProductCompareViewDto lowestMinAmountProduct = products.get(0);
        ProductCompareViewDto easiestMobileProduct = null;
        ProductCompareViewDto branchOnlyProduct = null;

        int depositCount = 0;
        int savingsCount = 0;

        for (ProductCompareViewDto product : products) {
            if (product == null) {
                continue;
            }

            if (product.getMax_interest_rate() > highestMaxRateProduct.getMax_interest_rate()) {
                highestMaxRateProduct = product;
            }

            if (product.getMin_join_amount() < lowestMinAmountProduct.getMin_join_amount()) {
                lowestMinAmountProduct = product;
            }

            if ("Y".equals(product.getMobile_join_yn()) && easiestMobileProduct == null) {
                easiestMobileProduct = product;
            }

            if ("Y".equals(product.getBranch_join_yn())
                    && !"Y".equals(product.getInternet_join_yn())
                    && !"Y".equals(product.getMobile_join_yn())
                    && branchOnlyProduct == null) {
                branchOnlyProduct = product;
            }

            if ("DEPOSIT".equals(product.getProduct_type())) {
                depositCount++;
            } else if ("SAVINGS".equals(product.getProduct_type())) {
                savingsCount++;
            }
        }

        StringBuilder summary = new StringBuilder();

        summary.append("선택한 상품은 총 ")
                .append(products.size())
                .append("개이며, ");

        if (depositCount > 0 && savingsCount > 0) {
            summary.append("예금과 적금 상품이 함께 비교되고 있습니다. ");
        } else if (depositCount > 0) {
            summary.append("예금 상품 중심으로 비교되고 있습니다. ");
        } else if (savingsCount > 0) {
            summary.append("적금 상품 중심으로 비교되고 있습니다. ");
        }

        summary.append("최고금리 기준으로는 ")
                .append(highestMaxRateProduct.getProduct_name())
                .append("이/가 연 ")
                .append(highestMaxRateProduct.getMax_interest_rate())
                .append("%로 가장 높습니다. ");

        summary.append("최소 가입금액 기준으로는 ")
                .append(lowestMinAmountProduct.getProduct_name())
                .append("이/가 ")
                .append(formatMoney(lowestMinAmountProduct.getMin_join_amount()))
                .append("부터 가입 가능해 접근성이 좋습니다. ");

        if (easiestMobileProduct != null) {
            summary.append("또한 ")
                    .append(easiestMobileProduct.getProduct_name())
                    .append("은/는 모바일 가입이 가능해 비대면 가입을 원하는 고객에게 적합합니다. ");
        }

        if (branchOnlyProduct != null) {
            summary.append(branchOnlyProduct.getProduct_name())
                    .append("은/는 영업점 중심 상품이므로 상담을 통한 가입을 선호하는 경우에 확인해볼 만합니다. ");
        }

        summary.append("금리만 보고 선택하기보다는 가입금액, 가입기간, 우대조건, 가입채널을 함께 비교한 뒤 선택하는 것이 좋습니다.");

        return summary.toString();
    }

    /**
     * 개별 상품 요약 fallback
     */
    public String createFallbackCompareProductSummary(ProductCompareViewDto product) {
        if (product == null) {
            return "요약할 상품 정보가 없습니다.";
        }

        String productType = getProductTypeLabel(product);
        String productName = product.getProduct_name();

        StringBuilder summary = new StringBuilder();

        summary.append(productName)
                .append("은/는 ")
                .append(productType)
                .append(" 상품입니다. ");

        summary.append(buildRatePoint(product));
        summary.append(buildAmountPoint(product));
        summary.append(buildChannelPoint(product));
        summary.append(buildConditionPoint(product));

        return summary.toString();
    }

    private String buildRatePoint(ProductCompareViewDto product) {
        double minRate = product.getMin_interest_rate();
        double maxRate = product.getMax_interest_rate();
        double gap = maxRate - minRate;

        StringBuilder text = new StringBuilder();

        if (maxRate >= 5.0) {
            text.append("최고금리가 연 ")
                    .append(maxRate)
                    .append("% 수준이라 금리 매력이 강한 편입니다. ");
        } else if (maxRate >= 3.0) {
            text.append("최고금리는 연 ")
                    .append(maxRate)
                    .append("%로, 우대조건을 챙겼을 때 장점이 커지는 상품입니다. ");
        } else {
            text.append("최고금리는 연 ")
                    .append(maxRate)
                    .append("%로 비교적 안정적인 기본형 상품에 가깝습니다. ");
        }

        if (gap >= 1.0) {
            text.append("최저금리와 최고금리 차이가 있어 우대조건 충족 여부가 중요합니다. ");
        } else {
            text.append("최저금리와 최고금리 차이가 크지 않아 조건 변동 부담은 비교적 낮은 편입니다. ");
        }

        return text.toString();
    }

    private String buildAmountPoint(ProductCompareViewDto product) {
        long minAmount = product.getMin_join_amount();

        if (minAmount <= 10000) {
            return "최소 가입금액이 " + formatMoney(minAmount) + "이라 소액으로 시작하기 좋습니다. ";
        }

        if (minAmount <= 1000000) {
            return "최소 가입금액은 " + formatMoney(minAmount) + "으로 일반적인 목돈 운용에 적합합니다. ";
        }

        return "최소 가입금액이 " + formatMoney(minAmount) + "이라 어느 정도 자금이 준비된 고객에게 더 적합합니다. ";
    }

    private String buildChannelPoint(ProductCompareViewDto product) {
        boolean branch = "Y".equals(product.getBranch_join_yn());
        boolean internet = "Y".equals(product.getInternet_join_yn());
        boolean mobile = "Y".equals(product.getMobile_join_yn());

        if (mobile && !branch && !internet) {
            return "가입채널은 모바일 중심이라 비대면 가입을 원하는 고객에게 잘 맞습니다. ";
        }

        if (mobile && (branch || internet)) {
            return "모바일 가입도 가능해서 접근성이 좋고, 다른 채널과 함께 선택지가 넓은 편입니다. ";
        }

        if (branch && !internet && !mobile) {
            return "영업점 가입 중심 상품이라 직원 상담을 받고 가입하려는 고객에게 적합합니다. ";
        }

        if (internet && !mobile) {
            return "인터넷 가입이 가능해 온라인으로 상품을 확인하고 가입하려는 경우에 활용하기 좋습니다. ";
        }

        return "가입채널은 상품 안내에서 한 번 더 확인하는 것이 좋습니다. ";
    }

    private String buildConditionPoint(ProductCompareViewDto product) {
        String condition = product.getCondition_note();

        if (condition == null || condition.trim().isEmpty() || "-".equals(condition.trim())) {
            return "우대조건 정보가 많지 않으므로 기본금리와 가입조건을 중심으로 판단하면 좋습니다.";
        }

        if (condition.contains("비과세")) {
            return "조건에는 비과세 관련 내용이 포함되어 있어 세제 혜택 대상 여부를 함께 확인하는 것이 좋습니다.";
        }

        if (condition.contains("급여") || condition.contains("자동이체") || condition.contains("카드")) {
            return "우대조건은 급여, 자동이체, 카드 사용 같은 거래 실적과 연결될 수 있으므로 본인의 거래 패턴과 맞는지 확인해야 합니다.";
        }

        if (condition.length() > 80) {
            return "우대조건이 비교적 세부적인 편이므로 가입 전 조건 충족 가능성을 꼼꼼히 확인하는 것이 좋습니다.";
        }

        return "우대조건을 충족할 수 있다면 기본 조건보다 더 유리하게 활용할 수 있는 상품입니다.";
    }

    private String getProductTypeLabel(ProductCompareViewDto product) {
        return "DEPOSIT".equals(product.getProduct_type()) ? "예금" : "적금";
    }

    private boolean isAiUnavailable(String aiSummary) {
        return aiSummary == null || aiSummary.trim().isEmpty();
    }

    private String formatMoney(long amount) {
        return String.format("%,d원", amount);
    }
}