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
import com.example.bnk.service.employees.staff.SuggestionService;

@RestController
@RequestMapping("/api/employee/suggestion")
public class SuggestionReviewApiController {
	
	@Autowired
	private EmployeeListService empService;
	@Autowired
	private SuggestionService sugService;
	
	
	// 나에게 온 제안서 리스트
	@GetMapping("/suggestionList")
	public List<SuggestionListDto> suggestionList(
			@AuthenticationPrincipal String username
			) {
		
		// 1. 내정보 가져오기 
		EmployeeDto myInfo = empService.findByUsername(username);
		
		// 2. 제안서 가져오기 
		List<SuggestionListDto> list = sugService.mySuggestionList(myInfo.getEmployee_no());
		
		
		
		return list;
	}
	
	// 제안서 상세보기
	@GetMapping("/suggestionReview")
	public SuggestionListDto suggestionReview(
			@RequestParam("suggestion_no")long suggestion_no
			) {
		
		//제안서 번호로 검색 >> 작성한 사원의 정보 포함
		SuggestionListDto view = sugService.suggestionReview(suggestion_no);
		
		return view;
	}
	// 제안서 승인
	@PostMapping("/approveSuggestion")
	public int approveSuggestion(
			@RequestBody ProductSuggestionDto dto
			) {
		int result = sugService.approveSuggestion(dto.getSuggestion_no());
		return result;
	}
	
	
	//rejectSuggestion
	// 제안서 거부
	@PostMapping("/rejectSuggestion")
	public int rejectSuggestion(
			@RequestBody ProductSuggestionDto dto
			) {
		int result = sugService.rejectSuggestion(dto.getSuggestion_no(), dto.getReject_reason());
		return result;
	}
	
}
