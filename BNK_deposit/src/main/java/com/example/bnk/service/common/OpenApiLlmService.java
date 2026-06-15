package com.example.bnk.service.common;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenApiLlmService {

    private final RestClient restClient;

    @Value("${openai.model}")
    private String model;

    public OpenAiLlmService(@Value("${openai.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String generateDictionaryAnswer(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", "너는 예적금 금융용어사전 챗봇이다. 반드시 제공된 DB 검색 결과만 사용해서 답변한다. DB 내용에 없는 정보는 추측하지 않는다. 답변은 한국어로 쉽고 친절하게 작성한다."
                                        )
                                )
                        ),
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        )
                                )
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