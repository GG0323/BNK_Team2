package com.example.bnk.service.product.ai;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductListViewDto;

@Component
public class ProductAiPromptBuilder {

    /**
     * 상품 추천 이유 생성용 프롬프트
     */
    public String buildRecommendPrompt(ProductListViewDto product) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("너는 은행 예금/적금 상품을 추천해주는 금융 상담 AI야.\n");
        prompt.append("아래 상품 정보를 보고 고객에게 보여줄 추천 이유를 한국어로 작성해줘.\n");
        prompt.append("조건:\n");
        prompt.append("- 2~3문장으로 짧게 작성\n");
        prompt.append("- 너무 과장하지 말 것\n");
        prompt.append("- 금리, 상품 유형, 모바일 가입 가능 여부를 자연스럽게 언급\n");
        prompt.append("- 전문적인 말투보다 일반 고객이 이해하기 쉬운 말투 사용\n\n");

        prompt.append("[상품 정보]\n");
        prompt.append("상품명: ").append(product.getProduct_name()).append("\n");
        prompt.append("상품유형: ").append(product.getProduct_type()).append("\n");
        prompt.append("최저금리: ").append(product.getMin_interest_rate()).append("\n");
        prompt.append("최고금리: ").append(product.getMax_interest_rate()).append("\n");
        prompt.append("모바일가입 가능여부: ").append(product.getMobile_join_yn()).append("\n");

        return prompt.toString();
    }

    /**
     * 비교 페이지에서 상품 1개 요약용 프롬프트
     */
    public String buildCompareProductSummaryPrompt(ProductCompareViewDto product) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("너는 은행 예금/적금 상품을 쉽게 설명해주는 금융 상담 AI야.\n");
        prompt.append("아래 상품 하나를 고객에게 보여줄 수 있도록 한국어로 요약해줘.\n");
        prompt.append("조건:\n");
        prompt.append("- 2~3문장으로 짧게 작성\n");
        prompt.append("- 상품 유형, 금리, 가입 방법, 우대조건을 중심으로 설명\n");
        prompt.append("- 장점만 말하지 말고 확인해야 할 점도 자연스럽게 포함\n");
        prompt.append("- 확정적인 투자 조언처럼 말하지 말고 참고용 안내처럼 말해줘\n\n");

        prompt.append("[상품 정보]\n");
        prompt.append("상품명: ").append(product.getProduct_name()).append("\n");
        prompt.append("상품유형: ").append(product.getProduct_type()).append("\n");
        prompt.append("최저금리: ").append(product.getMin_interest_rate()).append("\n");
        prompt.append("최고금리: ").append(product.getMax_interest_rate()).append("\n");
        prompt.append("가입방법 설명: ").append(product.getJoin_method_desc()).append("\n");
        prompt.append("영업점 가입 가능여부: ").append(product.getBranch_join_yn()).append("\n");
        prompt.append("인터넷 가입 가능여부: ").append(product.getInternet_join_yn()).append("\n");
        prompt.append("모바일 가입 가능여부: ").append(product.getMobile_join_yn()).append("\n");
        prompt.append("우대조건: ").append(product.getCondition_note()).append("\n");
        prompt.append("예금자보호 여부: ").append(product.getDepositor_protection_yn()).append("\n");

        return prompt.toString();
    }

    /**
     * 상품 비교 요약용 프롬프트
     */
    public String buildComparePrompt(List<ProductCompareViewDto> products) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("너는 은행 예금/적금 상품을 비교해주는 금융 상담 AI야.\n");
        prompt.append("아래 상품들을 비교해서 고객이 이해하기 쉬운 한국어 요약을 작성해줘.\n");
        prompt.append("조건:\n");
        prompt.append("- 4~6문장 정도로 작성\n");
        prompt.append("- 금리 차이, 가입 방식, 상품 유형 차이를 중심으로 설명\n");
        prompt.append("- 어떤 목적의 고객에게 어떤 상품이 더 적합한지 말해줘\n");
        prompt.append("- 확정적인 투자 조언처럼 말하지 말고, 참고용 안내처럼 말해줘\n\n");

        prompt.append("[비교 상품 목록]\n");

        for (ProductCompareViewDto product : products) {
            prompt.append("- 상품명: ").append(product.getProduct_name()).append("\n");
            prompt.append("  상품유형: ").append(product.getProduct_type()).append("\n");
            prompt.append("  최저금리: ").append(product.getMin_interest_rate()).append("\n");
            prompt.append("  최고금리: ").append(product.getMax_interest_rate()).append("\n");
            prompt.append("  모바일가입 가능여부: ").append(product.getMobile_join_yn()).append("\n");
            prompt.append("  가입방법 설명: ").append(product.getJoin_method_desc()).append("\n");
            prompt.append("  우대조건: ").append(product.getCondition_note()).append("\n");
            prompt.append("\n");
        }

        return prompt.toString();
    }
}