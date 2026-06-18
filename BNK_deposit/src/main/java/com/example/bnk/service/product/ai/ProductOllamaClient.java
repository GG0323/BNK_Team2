package com.example.bnk.service.product.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.bnk.dto.product.ai.ProductOllamaRequestDto;
import com.example.bnk.dto.product.ai.ProductOllamaResponseDto;

@Component
public class ProductOllamaClient {

    private final RestClient restClient;

    @Value("${ollama.product.model:qwen2.5:3b}")
    private String model;

    public ProductOllamaClient(
            @Value("${ollama.product.base-url:http://localhost:11434}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Ollama /api/generate 호출
     */
    public String generate(String prompt) {
        try {
            ProductOllamaRequestDto requestDto =
                    new ProductOllamaRequestDto(model, prompt, false);

            ProductOllamaResponseDto responseDto = restClient.post()
                    .uri("/api/generate")
                    .body(requestDto)
                    .retrieve()
                    .body(ProductOllamaResponseDto.class);

            if (responseDto == null || responseDto.getResponse() == null) {
                return "AI 응답을 생성하지 못했습니다.";
            }

            return responseDto.getResponse().trim();

        } catch (Exception e) {
            return "AI 서버 연결에 실패했습니다. Ollama 실행 상태를 확인해주세요.";
        }
    }
}