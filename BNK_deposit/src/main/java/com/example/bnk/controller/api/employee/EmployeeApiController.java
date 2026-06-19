package com.example.bnk.controller.api.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.auth.EmployeeDetails;
import com.example.bnk.dto.employee.EmployeeRegistDto;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.employees.EmployeeRegistService;
import com.example.bnk.service.employees.EmployeesLoginService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeApiController {
	
	@Autowired
	EmployeesLoginService loginService;
	@Autowired
	EmployeeRegistService registService;
	@Autowired
	EmployeeLogService logService;
	//logService.build(  "INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.");
	
	
	// 회원가입 처리 
	@PostMapping("/HRM/regist")
	public ResponseEntity<String> regist(
			EmployeeRegistDto empRegistDto,
			@RequestParam("img") MultipartFile img,		// 이미지는 dto에서 빼기
			@AuthenticationPrincipal EmployeeDetails employeeD
			) {
		System.out.println();
		
		// 컨트롤러 안에 붙어서 log를 하드코딩한다. >> 필터단에 이 값을 넘긴다.
		logService.build(employeeD.getUsername(),"INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.");
		
		System.out.println(empRegistDto.toString());
		if(!img.isEmpty() ) {
			System.out.println(img.getOriginalFilename());
		}
		
		//회원가입 처리 서비스 호출 파라미터 dto, 멀티파트
		int result = registService.regist(empRegistDto, img);
		
		if(result == 1) {
			return ResponseEntity.ok("등록 성공");
		}
		
		return ResponseEntity.ok("등록 실패");
	}
	
	
	
}
