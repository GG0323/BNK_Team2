package com.example.bnk.controller.Employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bnk.service.Employees.EmployeesLoginService;

@Controller
@RequestMapping("/EmployeesFunction")
public class EmployeesFunctionController {
	
	@Autowired
	EmployeesLoginService login;
	
//	@PostMapping("/login")
//	public String login(
//			@RequestParam("login_id")String login_id,
//			@RequestParam("password")String password
//			) {
//		
//		
//		login.login(login_id, password);
//		
//		
//		// 로그인 아이디로 회원 테이블 조사, 테이블의 결과의 암호화된 값을 가져온다. 
//		// 사용자가 입력한 비밀번호를 암호화하여 같은지 대조하고 결과가 참리가면 작함에 맞는 페이지로 리톤한다.
//		
////		if() {
////			
////			
////		}else {
////			return "redirect:/Employees/toMain+++ 실패 알림";
////		}
//		
//	}
//	
	
	
	
}
