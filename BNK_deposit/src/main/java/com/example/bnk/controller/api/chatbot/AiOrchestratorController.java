package com.example.bnk.controller.api.chatbot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.bnk.service.chatbot.AiRoutingService;

@RestController
@RequestMapping("/api/orchestrator")

@CrossOrigin(
        originPatterns = {
                "http://localhost:*",
                "https://localhost:*",
                "http://127.0.0.1:*",
                "https://127.0.0.1:*",
                "http://192.168.*.*:*",
                "https://192.168.*.*:*"
        },
        allowCredentials = "true"
)
public class AiOrchestratorController {

    private static final String FALLBACK_ANSWER =
            "죄송합니다. 해당 금융 AI 서비스 모듈이 준비 중이거나 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.";

    @Autowired
    private AiRoutingService aiRoutingService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload == null ? "" : payload.getOrDefault("message", "");
        String intent = aiRoutingService.determineRoutingIntent(userMessage);
        if (intent == null || intent.isBlank()) {
            intent = "DEFAULT";
        }
        String finalAnswer;

        switch (intent) {
            case "COMPARE":
                finalAnswer = callTargetAiServer(fastApiUrl("/fast/api/ai/2/product/compare"), userMessage);
                break;
            case "RECOMMEND":
                finalAnswer = callTargetAiServer(fastApiUrl("/fast/api/ai/2/product/recommend"), userMessage);
                break;
            case "DICTIONARY":
                finalAnswer = callTargetAiServer(fastApiUrl("/fast/api/ai/2/dictionary"), userMessage);
                break;
            case "FAQ":
                finalAnswer = callTargetAiServer(fastApiUrl("/fast/api/ai/2/faq"), userMessage);
                break;
            default:
                finalAnswer = "죄송합니다. 현재 준비중입니다.";
                intent = "DEFAULT";
                break;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("answer", aiRoutingService.determineRoutingIntent2(finalAnswer));

        return ResponseEntity.ok(result);
    }

    private String callTargetAiServer(String url, String message) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", message);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

            if (response == null) {
                return "AI 서버에서 응답을 받지 못했습니다.";
            }

            Object answer = firstPresent(response, "answer", "summary", "message", "detail");

            return answer != null ? answer.toString() : "AI 서버 응답 형식을 확인해주세요.";
        } catch (Exception e) {
            return FALLBACK_ANSWER;
        }
    }

    private Object firstPresent(Map<String, Object> response, String... keys) {
        for (String key : keys) {
            Object value = response.get(key);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String fastApiUrl(String path) {
        String baseUrl = fastApiBaseUrl == null ? "http://localhost:8000" : fastApiBaseUrl.trim();

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + path;
    }
}
