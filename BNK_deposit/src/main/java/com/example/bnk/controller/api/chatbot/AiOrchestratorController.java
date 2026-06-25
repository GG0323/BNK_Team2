package com.example.bnk.controller.api.chatbot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "*") // 플러터 앱이나 웹 프론트에서 통신할 수 있도록 CORS 허용
public class AiOrchestratorController {

    @Autowired
    private AiRoutingService aiRoutingService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        
        /*
         * handleChat(@RequestBody Map<String, Object > payload
        // message는 문자열로 꺼냄 (payload 타입이 Object라 캐스팅)
        String userMessage = payload.get("message") != null ? payload.get("message").toString() : "";
        // history 꺼내기 (프론트가 안 보내면 빈 리스트 → 단발 처리, 안 터짐)
        Object historyObj = payload.get("history");
        List<Object> history = (historyObj instanceof List)
                ? (List<Object>) historyObj
                : new ArrayList<>();
        */
        
        
        // 1단계: 장진우 담당 라우팅 엔진으로 의도(Intent) 파악
        String intent = aiRoutingService.determineRoutingIntent(userMessage);
        
        String finalAnswer;
        
        
        // 여기서 각 파트 맡은 부분에 따라서 자기 호출 주소 작성해넣으시면 됩니다.
        // 예시포트 그런거 생각하실 필요 없고, 이왕이면 자기가 만든 챗봇이 뭐 넘겨줄 때 json 형식으로 넘겨주는게 더 좋아요.
        // 2단계: 파악된 의도에 따라 담당 팀원의 AI 마이크로서비스 API 호출
        switch (intent) {
            case "COMPARE":
                // 상품 비교 AI 서버 호출 (예시 포트: 8081) -> 안만든대요..
                finalAnswer = callTargetAiServer("http://localhost:8081/api/ai/compare", userMessage);
                System.out.println("COMPARE");
                break;
                
            case "RECOMMEND":
                // 상품 추천 AI 서버 호출 (예시 포트: 8081) -> 안만든대요..
                finalAnswer = callTargetAiServer("http://localhost:8081/api/ai/recommend", userMessage);
                System.out.println("RECOMMEND");
                break;
                
            case "DICTIONARY":
                // 금융용어사전 AI 서버 호출 (예시 포트: 8082)
                finalAnswer = callTargetAiServer("http://192.168.0.87:8000/fast/api/ai/2/dictionary", userMessage);
                System.out.println("DICTIONARY");
                break;
                
            case "FAQ":
                // FAQ 벡터 검색 AI 서버 호출 (예시 포트: 8083)
            	//finalAnswer = callFaqAiServer("http://192.168.0.87:8000/fast/api/ai/2/faq", userMessage, history);
                finalAnswer = callTargetAiServer("http://192.168.0.87:8000/fast/api/ai/2/faq", userMessage);
                System.out.println("FAQ");
                break;
                
            default:
                finalAnswer = "죄송합니다. 현재 준비중입니다.";
                System.out.println("오류");
                break;
        }

        // 3단계: 클라이언트(웹 프론트 및 플러터)가 받기 좋은 규격으로 리턴
        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);       // 어떤 AI가 일했는지 트래킹용
        result.put("answer", finalAnswer);   // 실제 유저가 볼 답변
        
        return ResponseEntity.ok(result);
    }

    // 팀원들의 AI 서버에 HTTP POST 요청을 보내고 답변을 받아오는 포워딩 헬퍼 함수
    private String callTargetAiServer(String url, String message) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", message);
            
            // 팀원들에게 인풋은 {"query": "내용"}으로 맞춰달라고 약속해두면 편합니다.
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return response.get("answer").toString();
            
        } catch (Exception e) {
            return "죄송합니다. 해당 금융 AI 서비스 모듈이 준비 중이거나 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.";
        }
    }
    
    /*
    // FAQ 전용 헬퍼: query + history를 함께 보냄 (멀티턴)
    private String callFaqAiServer(String url, String message, List<Object> history) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", message);
            requestBody.put("history", history);  // FastAPI의 history 필드로 전달

            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return response.get("answer").toString();

        } catch (Exception e) {
            return "죄송합니다. 해당 금융 AI 서비스 모듈이 준비 중이거나 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.";
        }
    }
    */
    
}