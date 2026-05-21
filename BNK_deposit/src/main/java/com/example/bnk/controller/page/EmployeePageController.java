package com.example.bnk.controller.page;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bnk.service.Employees.EmployeeLogService;

@Controller
@RequestMapping("/employee")
public class EmployeePageController {
	
	@Autowired
	EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	
	// /employee/toMain
	@GetMapping("/toMain") 
	public String mainWorkSpace() {
		return "Employees/mainWorkspaceLogin";
	}
	
	// /employee/manager/HRM/hrmRegist
	@GetMapping("/manager/HRM/hrmRegist")  
	public String hrmRegist() {
		logService.build("PAGEVIEW", null, null, "페이지간 이동을 실현한다: 인사관리/신규 사원 등록", "GET", "/employee/manager/HRM/hrmRegist");
		return "Employees/manager/HRM/hrmRegist";
	}
	// /employee/manager/LOG/logList
	@GetMapping("/manager/LOG/logList")
	public String logList() {
		logService.build("PAGEVIEW", null, null, "페이지간 이동을 실현한다: 로그/목록 보기", "GET", "/employee/manager/LOG/logList");
		return "Employees/manager/LOG/logList";
	}
	
	
	
	
	
	
	
}
