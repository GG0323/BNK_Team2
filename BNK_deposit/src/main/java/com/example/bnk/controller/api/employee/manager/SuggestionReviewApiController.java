package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.product.ProductSuggestionDto;
import com.example.bnk.dto.product.suggestion.SuggestionListDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.employees.staff.SuggestionService;

@RestController
@RequestMapping("/api/employee/suggestion")
public class SuggestionReviewApiController {
	
	@Autowired
	private EmployeeListService empService;
	@Autowired
	private SuggestionService sugService;
	@Autowired
	private EmployeeLogService logService;
	
	// 나에게 온 제안서 리스트
	@GetMapping("/suggestionList")
	public List<SuggestionListDto> suggestionList(
			@AuthenticationPrincipal String username
			) {
		
		// 1. 내정보 가져오기 
		EmployeeDto myInfo = empService.findByUsername(username);
		
		//로그 
		String logKey = "사원 번호:"+myInfo.getEmployee_no();
		logService.build(username, "SELECT", "TB_PRODUCTS_SUGGESTION", logKey, " 제안서 목록을 불러온다.");
		// 2. 제안서 가져오기 
		List<SuggestionListDto> list = sugService.mySuggestionList(myInfo.getEmployee_no());
		
		
		
		return list;
	}
	
	// 제안서 상세보기
	@GetMapping("/suggestionReview")
	public SuggestionListDto suggestionReview(
			@RequestParam("suggestion_no")long suggestion_no,
			@AuthenticationPrincipal String username
			) {
		//로그
		String logKey = "제안서 번호:"+suggestion_no;
		logService.build(username, "SELECT", "TB_PRODUCTS_SUGGESTION", logKey, " 제안서 상세를 불러온다.");
		
		//제안서 번호로 검색 >> 작성한 사원의 정보 포함
		SuggestionListDto view = sugService.suggestionReview(suggestion_no);
		
		return view;
	}
	// 제안서 승인
	@PostMapping("/approveSuggestion")
	public int approveSuggestion(
			@RequestBody ProductSuggestionDto dto,
			@AuthenticationPrincipal String username
			) {
		//로그
		String logKey = "제안서 번호:"+dto.getSuggestion_no();
		logService.build(username, "APPROVE", "TB_PRODUCTS_SUGGESTION", logKey, " 제안서 승인.");

		int result = sugService.approveSuggestion(dto.getSuggestion_no());
		return result;
	}
	
	
	//rejectSuggestion REJECT 
	// 제안서 거부
	@PostMapping("/rejectSuggestion")
	public int rejectSuggestion(
			@RequestBody ProductSuggestionDto dto,
			@AuthenticationPrincipal String username
			) {
		//로그
		String logKey = "제안서 번호:"+dto.getSuggestion_no();
		logService.build(username, "REJECT", "TB_PRODUCTS_SUGGESTION", logKey, " 제안서 반려.");
		
		int result = sugService.rejectSuggestion(dto.getSuggestion_no(), dto.getReject_reason());
		return result;
	}
	
}
