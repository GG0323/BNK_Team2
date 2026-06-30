package com.example.bnk.service.chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiRoutingService {

    private final RestTemplate restTemplate;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    // GPT-5 Nano Mini 모델명 명시
    private final String MODEL_NAME = "gpt-5-nano";
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public AiRoutingService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 🎯 사용자의 질문을 분석하여 어떤 AI 모듈로 보낼지 결정하는 라우팅 함수 (순수 HTTP 통신 버전)
     */
    public String determineRoutingIntent(String userMessage) {
        
        String systemPrompt = 
            "너는 BNK 부산은행의 챗봇 시스템을 총괄하는 'AI 오케스트레이터 라우팅 엔진'이다.\n" +
            "너의 유일한 임무는 사용자의 질문을 분석하여 아래의 4가지 카테고리 중 딱 하나만 골라 대문자로 출력하는 것이다.\n\n" +
            "1. COMPARE : 금융 상품들을 '비교'하거나 분석해달라고 요청하는 경우\n" +
            "2. RECOMMEND : 맞춤형 상품을 '추천'해달라고 하는 경우\n" +
            "3. DICTIONARY : 어려운 '금융 용어'의 뜻을 물어보거나 즉시 풀이를 원하는 경우\n" +
            "4. FAQ : 자주 묻는 질문 관련 '업무 안내'인 경우\n\n" +
            "오직 딱 한 단어(COMPARE, RECOMMEND, DICTIONARY, FAQ, DEFAULT)만 출력하라. 다른 부연설명은 하지 마라.";

        try {
            // 1. OpenAI 표준 규격에 맞게 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 2. OpenAI Chat Completion 규격 데이터 본문(Body) 빌드
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. OpenAI 서버로 직접 POST 통신 요청 날리기
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 4. 복잡한 OpenAI JSON 결과 트리에서 정답 단어만 쏙 파싱하기
                List choices = (List) response.getBody().get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map messageResult = (Map) firstChoice.get("message");
                String intentResult = (String) messageResult.get("content");

                return intentResult.trim().toUpperCase();
            }
            
            return "DEFAULT";

        } catch (Exception e) {
            System.err.println("OpenAI Direct API 통신 에러: " + e.getMessage());
            return "DEFAULT"; 
        }
    }
    
public String determineRoutingIntent2(String aiMessage) {
        
        String systemPrompt = 
            "너는 답변을 다듬어주는 'AI 언어 엔진'이다." + 
        	"너에게 주어진 답변을 대답형식으로 다듬어라." + 
            "말은 무조건 높임말로 바꾸어서 출력할 것."+
        	"만약 '죄송합니다. 현재 준비중입니다.'라는 답변을 받으면 그대로 출력하면 된다.";

        try {
            // 1. OpenAI 표준 규격에 맞게 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 2. OpenAI Chat Completion 규격 데이터 본문(Body) 빌드
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", aiMessage));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. OpenAI 서버로 직접 POST 통신 요청 날리기
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 4. 복잡한 OpenAI JSON 결과 트리에서 정답 단어만 쏙 파싱하기
                List choices = (List) response.getBody().get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map messageResult = (Map) firstChoice.get("message");
                String intentResult = (String) messageResult.get("content");

                return intentResult.trim().toUpperCase();
            }
            
            return "DEFAULT";

        } catch (Exception e) {
            System.err.println("OpenAI Direct API 통신 에러: " + e.getMessage());
            return "DEFAULT"; 
        }
    }
}