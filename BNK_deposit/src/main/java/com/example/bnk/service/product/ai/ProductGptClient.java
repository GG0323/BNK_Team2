package com.example.bnk.service.product.ai;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductGptClient {

    private final RestClient restClient;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-5-nano}")
    private String model;

    public ProductGptClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(6));
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .requestFactory(requestFactory)
                .build();
    }

    public String generate(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return null;
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("*")) {
            return null;
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "instructions", buildSystemInstruction(),
                    "input", prompt,
                    "max_output_tokens", 600
            );

            Map<?, ?> responseBody = restClient.post()
                    .uri("/v1/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractOutputText(responseBody);

        } catch (Exception e) {
            System.err.println("Product GPT API error: " + e.getMessage());
            return null;
        }
    }

    private String buildSystemInstruction() {
        return "너는 BNK 부산은행 예금/적금 상품 비교를 도와주는 금융 상담 AI다. "
                + "고객에게 보여줄 문장을 한국어로 작성한다. "
                + "과장된 표현이나 확정적인 투자 조언은 피하고, 상품설명서 확인이 필요하다는 뉘앙스를 유지한다. "
                + "출력은 3~6문장 정도로 간결하게 작성한다.";
    }

    @SuppressWarnings("unchecked")
    private String extractOutputText(Map<?, ?> responseBody) {
        if (responseBody == null) {
            return null;
        }

        Object outputText = responseBody.get("output_text");
        if (outputText instanceof String text && !text.trim().isEmpty()) {
            return text.trim();
        }

        Object output = responseBody.get("output");
        if (!(output instanceof List<?> outputList)) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (Object outputItem : outputList) {
            if (!(outputItem instanceof Map<?, ?> outputMap)) {
                continue;
            }

            Object content = outputMap.get("content");
            if (!(content instanceof List<?> contentList)) {
                continue;
            }

            for (Object contentItem : contentList) {
                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object text = contentMap.get("text");
                if (text instanceof String textValue && !textValue.trim().isEmpty()) {
                    result.append(textValue.trim()).append("\n");
                }
            }
        }

        String text = result.toString().trim();
        return text.isEmpty() ? null : text;
    }
}