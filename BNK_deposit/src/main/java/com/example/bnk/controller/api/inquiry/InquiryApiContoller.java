package com.example.bnk.controller.api.inquiry;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.auth.MemberDetails;
import com.example.bnk.dto.inquiry.FaqCandidateDto;
import com.example.bnk.dto.inquiry.FaqDto;
import com.example.bnk.dto.inquiry.InquiryDto;
import com.example.bnk.dto.inquiry.InquiryMsgDto;
import com.example.bnk.dto.inquiry.InquiryMsgListDto;
import com.example.bnk.service.inquiry.FaqCandidateService;
import com.example.bnk.service.inquiry.FaqService;
import com.example.bnk.service.inquiry.InquiryService;

@RestController
@RequestMapping("/api/inquiry")
public class InquiryApiContoller {
	
	@Autowired
	private FaqService faqService;
	@Autowired
	private InquiryService inquiryService;
	@Autowired
	private FaqCandidateService candidateService;
	
	// faq 페이지 진입시 faq 목록을 불러오는 컨트롤러
	@GetMapping("/faq")
	public List<FaqDto> faqList(){
		
		List<FaqDto> faqlist = faqService.getFAQ();
		
		return faqlist;
	}
	
	
	
	// 문의사항 등록
	@PostMapping("/form")
	public ResponseEntity<String> form(
	        @RequestParam("INQUIRY_CATEGORY") String inquiryCategory,
	        @RequestParam("INQUIRY_TITLE") String inquiryTitle,
	        @RequestParam("MSG_CONTENT") String msgContent,
	        @AuthenticationPrincipal String username
	        ) {
		// ++ authentication 에서 유저 pk 뽑아오기, id 뽑아오기
		System.out.println("파라미터 확인 // " + inquiryCategory +", "+ inquiryTitle +", "+ msgContent );
		
		
    	//인서트 함수
        inquiryService.insertInquiry(inquiryCategory, inquiryTitle, msgContent, 25, "하드코딩-id");

        return ResponseEntity.ok("success");

	}
	
	// 유저 문의 사항 목록
	@GetMapping("/list")
	public List<InquiryDto> callList(
			//@AuthenticationPrincipal
			) {
		// authentication 에서 pk 뽑아오기
		
		List<InquiryDto> inquiryList = inquiryService.callList(25);
		
		return inquiryList;
	}
	
	// 문의 사항 상세
	@GetMapping("/detail")
	public ResponseEntity<InquiryMsgListDto> inquiryDetail(
			@RequestParam("inquiry_no") int inquiryNo
			) {
		System.out.println("inquiry_no: " + inquiryNo);
		// 응답 전용 dto 
		InquiryMsgListDto msgList = inquiryService.callMsg(inquiryNo);
		
		return ResponseEntity.ok(msgList);
	}
	
	
	// 추가 문의 사항 작성
	@PostMapping("/msg")
	public ResponseEntity<String> msg(
			@RequestBody InquiryMsgDto dto,
			@AuthenticationPrincipal String username
			) {
		inquiryService.addMsg(dto.getInquiry_no(), "USER", "하드코딩-id" , dto.getMsg_content());
		inquiryService.updateStatus(dto.getInquiry_no(), "접수완료");
		return ResponseEntity.ok("추가문의 등록 성공");
	}
	
	// 문의 만족 - 해결상태
	@GetMapping("/cleared")
	public ResponseEntity<String> cleared(
			@RequestParam("inquiry_no") int inquiryNo
			){
		
		inquiryService.updateStatus(inquiryNo, "해결");
		return ResponseEntity.ok("cleared");
	}
	
	//=================================
	
	// 직원이 보는 문의사항 전체 목록, 응답, 미응답 나누어보기 가능하도록
	@GetMapping("/waitingAnswers")
	public List<InquiryDto> waitingAnswers(){
		
		List<InquiryDto> listDto = inquiryService.waitingAnswers();
		
		return listDto;
	}
	//문의사항 조회 -> 상태 변경
	@GetMapping("/answer")
	@Transactional
	public ResponseEntity<InquiryMsgListDto> answer(
			@RequestParam("inquiry_no") long inquiryNo
			) {
		System.out.println("관리자 페이지 inquiry_no: " + inquiryNo);
		// 응답 전용 dto 
		InquiryMsgListDto msgList = inquiryService.callMsg(inquiryNo);
		
		// 문의 상태값 변경하는 함수
		if("접수완료".equals(msgList.getInquiry_status())) {
			inquiryService.updateStatus(inquiryNo, "처리중");
			msgList.setInquiry_status("처리중");
		}
		
		return ResponseEntity.ok(msgList);
	}
	
	//문의 사항 답변 -> 상태 변경
	@PostMapping("/postingAnswer")
	@Transactional
	public ResponseEntity<String> answer(
			@RequestBody InquiryMsgDto dto,
			@AuthenticationPrincipal String username
			) {
		//응답insert authentication id 꺼내기
		inquiryService.postingAnswer(dto.getInquiry_no(), "ADMIN", username , dto.getMsg_content());
		
		//상태 변경
		inquiryService.updateStatus(dto.getInquiry_no(), "답변완료");
		
		return ResponseEntity.ok("success");
	}

	
	//문의 사항 모아서 정리하기   /api/inquiry
	
	// faq후보 DB 테이블 조회   
	@GetMapping("/faqCandidates")
	public List<FaqCandidateDto> getPendingCandidates() {
        return candidateService.getPendingCandidates();
    }
	
	// api 호출버튼 빙글빙글 파이선에서 DB값을 조회하고 ai에 넘겨서 목록 만들고 DB에 저장
	@PostMapping("/refresh")
    public Map<String, Object> refresh() {
        return candidateService.triggerRefresh();
    }
	
	// 반려 버튼
	@PostMapping("/faqCandidates/{candidateNo}/reject")
	public  Map<String, Object> reject(
			@PathVariable("candidateNo") Long candidateNo
			) {
	    candidateService.rejectCandidate(candidateNo);
	    return Map.of("ok", true, "candidateNo", candidateNo);
	}
	
	// 승인 버튼
	@PostMapping("/faqCandidates/{candidateNo}/approve")
	public Map<String, Object> approve(
	        @PathVariable("candidateNo") Long candidateNo,
	        @RequestBody Map<String, String> req, 
	        @AuthenticationPrincipal MemberDetails memberD
			) {
	    String question = req.get("question");
	    String answer = req.get("answer");
	    long employeeNo = memberD.getPk();
	    return candidateService.approveCandidate(candidateNo, question, answer, employeeNo);
	}
	
	
	
	
}
