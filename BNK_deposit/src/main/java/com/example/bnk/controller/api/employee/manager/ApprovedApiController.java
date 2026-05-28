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
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.product.ApprovedSuggestionService;

@RestController
@RequestMapping("/api/employee/approved")
public class ApprovedApiController {
	
	@Autowired
	ApprovedSuggestionService approvedService;
	@Autowired
	private EmployeeListService empService;
	@Autowired
	private EmployeeLogService logService;
	
	// 승인한 제안서 목록 가져오기
	@GetMapping("/approvedList")
	public List<ApprovedSuggestionDetailDto> approvedList(
			@AuthenticationPrincipal String username
			) {
		
		// 1. 내정보 가져오기 
		EmployeeDto myInfo = empService.findByUsername(username);
		
		//로그
		String logKey = "사원 번호:"+myInfo.getEmployee_no();
		logService.build(username, "SELECT", "TB_APPROVED_SUGGESTION", logKey, "승인된 제안서 목록을 불러온다.");
		
		// 2. 내가 승인한 상품
		List<ApprovedSuggestionDetailDto> list = approvedService.approvedList(myInfo.getEmployee_no());
		
		return list;
	}
	
	// 승인한 제안서 상세
	@GetMapping("/approvedDetail")
	public ApprovedSuggestionDetailDto approvedDetail(
			@RequestParam("suggestion_no") long suggestion_no,
			@AuthenticationPrincipal String username
			) {
		
		ApprovedSuggestionDetailDto detail = approvedService.approvedDetail(suggestion_no);
		//로그
		String logKey = "제안서 번호:"+suggestion_no;
		logService.build(username, "SELECT", "TB_APPROVED_SUGGESTION", logKey, "승인된 제안서 목록을 불러온다.");
		
		return detail;
	}
	
	

}
