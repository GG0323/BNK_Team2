package com.example.bnk.service.inquiry;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.bnk.dao.inquiry.IFaqCandidateDao;
import com.example.bnk.dao.inquiry.IFaqDao;
import com.example.bnk.dto.inquiry.FaqCandidateDto;

@Service
public class FaqCandidateService {
	
	@Autowired
	private IFaqCandidateDao candidateDao;
	@Autowired
	private IFaqDao faqDao;
	
    // 스프링부트 3.2+ 면 RestClient, 아니면 RestTemplate 로 // 둘 다 자바에서 다른 서버(여기선 파이썬 FastAPI)를 HTTP로 부르는 도구
	// 파이선 api 불러오기
    private final RestClient restClient = RestClient.create("http://localhost:8000");
    //private final RestClient restClient = RestClient.create("http://192.168.0.87:8000");
    
    
    //http://127.0.0.1:8000/docs - 스워거 주소
    
    // /fast/api/ai/2/faqs -- add_faq
	
	/** 대기 후보 목록 조회 (오라클에서 직접) */
    public List<FaqCandidateDto> getPendingCandidates() {
    	System.out.println("faq후보군 불러오기");
    	List<FaqCandidateDto> list = candidateDao.selectPendingCandidates();
        return list;
    }
	
    // api 호출 부분
    /**
     * FastAPI 파이프라인 실행.
     * 동기 호출이라 파이썬이 끝날 때까지 대기(화면은 빙글빙글).
     * 반환은 요약 dict 예: {"ok":true, "new_candidates":5}
     */
    public Map<String, Object> triggerRefresh() {
        System.out.println("FAQ 후보 갱신 요청 → FastAPI 호출");
        
        
        /** Fast Api 호출 !!!! */
        return restClient.post()
                .uri("/fast/api/ai/2/faq/refresh")
                .retrieve()
                .body(Map.class);
    }
	
    
    // 후보테이블 상태값 변경
    public void rejectCandidate(Long candidateNo) {
        System.out.println("후보 반려: " + candidateNo);
        candidateDao.updateStatus(candidateNo, "반려");
    }
    
    
    // callAddFaq 함수를 호출한다.
    /** 후보 승인: 파이썬(FAQ 등록) 먼저 → 성공하면 오라클 상태변경 */
    public Map<String, Object> approveCandidate(Long candidateNo, String category, String question, String answer, long employeeNo) {
        System.out.println("후보 승인 시작: " + candidateNo);

        // 1) 파이썬 호출 (지금은 가짜)
        Map<String, Object> pyResult = callAddFaq(question, answer);

        // 2) ok 확인
        boolean ok = Boolean.TRUE.equals(pyResult.get("ok"));
        if (!ok) {
            // 파이썬 실패 → 오라클 안 건드림, 후보는 '대기'로 남음
            System.out.println("파이썬 실패 → 승인 중단");
            return Map.of("ok", false, "message", "FAQ 등록 실패. 다시 시도해주세요.");
        }

        // 3) Faq_candidate Dao 호출
        candidateDao.approveCandidate(candidateNo, "승인", answer);
        System.out.println("승인 완료: " + candidateNo);
        
        // 4) FaQ 호출 - 
        faqDao.insertNewFaq(question, answer, category, 0, employeeNo);
        
        return Map.of("ok", true, "candidateNo", candidateNo);
    }

    /** 파이썬 add_faq 호출.*/
    private Map<String, Object> callAddFaq(String question, String answer) {
        System.out.println("Fast API add_faq 호출: " + question);
        

        /** Fast Api 호출 !!!! */
        return restClient.post()
                .uri("/fast/api/ai/2/faqs")
                .body(Map.of("question", question, "answer", answer))
                .retrieve()
                .body(Map.class);
    }
    
    
    
    
    
    
    
    
    
    
    
    
}
