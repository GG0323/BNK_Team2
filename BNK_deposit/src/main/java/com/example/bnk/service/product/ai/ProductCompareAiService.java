package com.example.bnk.service.product.ai;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductCompareViewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCompareAiService {

    private final ProductAiPromptBuilder productAiPromptBuilder;
    private final ProductOllamaClient productOllamaClient;

    /**
     * 상품 비교 AI 요약 생성
     */
    public String createCompareSummary(List<ProductCompareViewDto> products) {
        if (products == null || products.isEmpty()) {
            return "비교할 상품 정보가 없습니다.";
        }

        String prompt = productAiPromptBuilder.buildComparePrompt(products);

        return productOllamaClient.generate(prompt);
    }

    /**
     * 비교 페이지에서 상품 1개 AI 요약 생성
     */
    public String createCompareProductSummary(ProductCompareViewDto product) {
        if (product == null) {
            return "요약할 상품 정보가 없습니다.";
        }

        String prompt = productAiPromptBuilder.buildCompareProductSummaryPrompt(product);

        return productOllamaClient.generate(prompt);
    }

    /**
     * Ollama 연결 실패 시에도 사용할 수 있는 기본 비교 요약
     */
    public String createFallbackCompareSummary(List<ProductCompareViewDto> products) {
        if (products == null || products.isEmpty()) {
            return "비교할 상품 정보가 없습니다.";
        }

        ProductCompareViewDto highestRateProduct = products.get(0);

        for (ProductCompareViewDto product : products) {
            if (product.getMax_interest_rate() > highestRateProduct.getMax_interest_rate()) {
                highestRateProduct = product;
            }
        }

        return "비교한 상품 중 최고 금리가 가장 높은 상품은 "
                + highestRateProduct.getProduct_name()
                + "입니다. 금리만 기준으로 보면 해당 상품을 우선 검토할 수 있지만, "
                + "실제 가입 시에는 가입 기간, 가입 금액, 모바일 가입 가능 여부도 함께 확인하는 것이 좋습니다.";
    }

    /**
     * Ollama 연결 실패 시에도 사용할 수 있는 상품 1개 기본 요약
     */
    public String createFallbackCompareProductSummary(ProductCompareViewDto product) {
        if (product == null) {
            return "요약할 상품 정보가 없습니다.";
        }

        String productType = "DEPOSIT".equals(product.getProduct_type()) ? "예금" : "적금";
        String mobileText = "Y".equals(product.getMobile_join_yn())
                ? "모바일 가입이 가능해 비대면으로 접근하기 좋은 상품입니다."
                : "모바일 가입 가능 여부는 제한적이므로 가입 채널을 확인하는 것이 좋습니다.";

        return product.getProduct_name()
                + "은/는 "
                + productType
                + " 상품이며, 최고 금리 "
                + product.getMax_interest_rate()
                + "%를 기준으로 비교해볼 수 있습니다. "
                + mobileText;
    }
}