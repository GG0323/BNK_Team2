package com.example.bnk.service.common;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

//@Service
public class DictionaryLlmService {

    private final RestClient restClient;

    @Value("${openai.model}")
    private String model;

    public DictionaryLlmService(@Value("${openai.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String generateDictionaryAnswer(String prompt) {
    	Map<String, Object> requestBody = Map.of(
    	        "model", model,
    	        "stream", false,
    	        "options", Map.of(
    	                "temperature", 0.2
    	        ),
    	        "messages", List.of(
    	                Map.of(
    	                        "role", "system",
    	                        "content",
    	                        "너는 예적금 금융용어사전 챗봇이다. " +
    	                        "반드시 제공된 DB 검색 결과만 사용해서 답변한다. " +
    	                        "DB 내용에 없는 정보는 절대 추측하지 않는다. " +
    	                        "답변은 한국어로 쉽고 자연스럽게 작성한다. " +
    	                        "비문이나 어색한 표현 없이 완성된 문장으로 답한다."
    	                ),
    	                Map.of(
    	                        "role", "user",
    	                        "content", prompt
    	                )
    	        )
    	);

        Map<?, ?> response = restClient.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    private String extractText(Map<?, ?> response) {
        if (response == null) {
            return "답변을 생성하지 못했습니다.";
        }

        Object outputObj = response.get("output");

        if (!(outputObj instanceof List<?> outputList)) {
            return "답변을 생성하지 못했습니다.";
        }

        StringBuilder result = new StringBuilder();

        for (Object outputItem : outputList) {
            if (!(outputItem instanceof Map<?, ?> outputMap)) {
                continue;
            }

            Object contentObj = outputMap.get("content");

            if (!(contentObj instanceof List<?> contentList)) {
                continue;
            }

            for (Object contentItem : contentList) {
                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object textObj = contentMap.get("text");

                if (textObj != null) {
                    result.append(textObj.toString());
                }
            }
        }

        if (result.length() == 0) {
            return "답변을 생성하지 못했습니다.";
        }

        return result.toString();
    }
}