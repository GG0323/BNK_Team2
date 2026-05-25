package com.example.bnk.controller.api.employee.staff;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.product.ProductSuggestionDto;
import com.example.bnk.dto.product.suggestion.SuggestionPageDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.employees.staff.SuggestionService;

@RestController
@RequestMapping("/api/staff/suggestion")
public class SuggestionApiController {
	
	@Autowired
	private SuggestionService sugService;
	@Autowired
	private EmployeeListService empService;
	
	//2. 특정 권한 이상인 사람들을 찾아서 반환 
	@GetMapping("/managers")
	public SuggestionPageDto managers(
			@AuthenticationPrincipal String username
			) {
		
		// username 으로 유저 정보 검색 ??
		EmployeeDto myInfo = empService.findByUsername(username);
		
		// 특정 권한 이상인 사람들을 찾아서 반환 
		List<EmployeeDto> empDto = empService.managers();
		
		SuggestionPageDto response = new SuggestionPageDto();
		response.setMyInfo(myInfo);
		response.setManagers(empDto);
		
		return response;
	}
	
	
	
	// 제안서를 DB에 등록하는 컨트롤러
	@PostMapping("/writeSuggestion")
	public int writeSuggestion(
			ProductSuggestionDto suggestionDto
			) {
		
		int result = sugService.writeSuggestion(suggestionDto);
		
		return result;
	}
	
	
	
	
	
	
	
	
	
	
}
