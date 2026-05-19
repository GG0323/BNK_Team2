package com.example.bnk.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.service.Employees.EmployeesLoginService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeApiController {
	
	@Autowired
	EmployeesLoginService loginService;
	
	//로그인 처리
	@PostMapping("login")
	private int login(
			@RequestParam("login_id")String login_id,
			@RequestParam("password")String password
			) {
		System.out.println(login_id+"직원~~~~~~~"+ password);
		
		//결과로 t/f 반환
		boolean login = loginService.login(login_id, password);
		
		System.out.println("결과 비교 ~~~~~ "+login);
		
		
		return 0;
	}
	
	
	
}
