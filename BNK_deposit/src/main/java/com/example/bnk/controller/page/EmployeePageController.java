package com.example.bnk.controller.page;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bnk.service.employees.EmployeeLogService;


@Controller
@RequestMapping("/employee")
public class EmployeePageController {
	
	@Autowired
	EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	
	// /employee/toMain
	@GetMapping("/toMain") 
	public String mainWorkSpace(Model model,
			@RequestParam(value = "message", required = false)String msg) {
		if(msg != null) {
			model.addAttribute("msg", msg);
		}
		
		return "Employees/mainWorkspaceLogin";
	}
	
	// /employee/manager/HRM/hrmRegist
	@GetMapping("/manager/HRM/hrmRegist")  
	public String hrmRegist() {
		logService.build("PAGEVIEW", null, null, "페이지간 이동을 실현한다: 인사관리/신규 사원 등록", "GET", "/employee/manager/HRM/hrmRegist");
		return "Employees/manager/HRM/hrmRegist";
	}
	
	// 직원 로그 조회 페이지 /employee/manager/LOG/logList
	@GetMapping("/manager/LOG/logList")
	public String logList() {
		logService.build("PAGEVIEW", null, null, "페이지간 이동을 실현한다: 로그/목록 보기", "GET", "/employee/manager/LOG/logList");
		return "Employees/manager/LOG/logList";
	}
	
	// 직원 리스트 페이지 
	// /employee/manager/HRM/hrmEmployeeList
	@GetMapping("/manager/HRM/hrmEmployeeList")
	public String hrmEmployeeList() {
		return "Employees/manager/HRM/hrmEmployeeList";
	}
	
	// /employee/manager/HRM/hrmEmployeeDetailList
	@GetMapping("/manager/HRM/hrmEmployeeDetailList")
	public String hrmEmployeeDetailList() {
		return "Employees/manager/HRM/hrmEmployeeDetailList";
	}
	
}