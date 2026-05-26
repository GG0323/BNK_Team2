package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.product.ApprovedSuggestionService;

@RestController
@RequestMapping("/api/employee/approved")
public class ApprovedApiController {
	
	@Autowired
	ApprovedSuggestionService approvedService;
	@Autowired
	private EmployeeListService empService;
	
	// 승인한 제안서 목록 가져오기
	@GetMapping("/approvedList")
	public List<ApprovedSuggestionDetailDto> approvedList(
			@AuthenticationPrincipal String username
			) {
		// 1. 내정보 가져오기 
		EmployeeDto myInfo = empService.findByUsername(username);
		
		// 2. 내가 승인한 상품
		List<ApprovedSuggestionDetailDto> list = approvedService.approvedList(myInfo.getEmployee_no());
		
		return list;
	}
	
	// 승인한 제안서 상세
	@GetMapping("/approvedDetail")
	public ApprovedSuggestionDetailDto approvedDetail(
			@RequestParam("suggestion_no") long suggestion_no
			) {
		
		ApprovedSuggestionDetailDto detail = approvedService.approvedDetail(suggestion_no);
		
		return detail;
	}
	
	

}
