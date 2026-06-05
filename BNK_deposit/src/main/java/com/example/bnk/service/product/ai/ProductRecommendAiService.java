package com.example.bnk.service.product.ai;

import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductListViewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductRecommendAiService {

    private final ProductAiPromptBuilder productAiPromptBuilder;
    private final ProductOllamaClient productOllamaClient;

    /**
     * 상품 추천 이유 생성
     */
    public String createRecommendReason(ProductListViewDto product) {
        String prompt = productAiPromptBuilder.buildRecommendPrompt(product);

        return productOllamaClient.generate(prompt);
    }

    /**
     * Ollama 연결 실패 시에도 사용할 수 있는 기본 추천 이유
     */
    public String createFallbackRecommendReason(ProductListViewDto product) {
        String productType = "DEPOSIT".equals(product.getProduct_type()) ? "예금" : "적금";
        String mobileText = "Y".equals(product.getMobile_join_yn())
                ? "모바일 가입이 가능해 접근성이 좋습니다."
                : "영업점 또는 다른 채널을 통해 가입을 확인해야 합니다.";

        return product.getProduct_name()
                + "은/는 "
                + productType
                + " 상품이며, 최고 금리 "
                + product.getMax_interest_rate()
                + "%를 기준으로 비교해볼 만한 상품입니다. "
                + mobileText;
    }
}