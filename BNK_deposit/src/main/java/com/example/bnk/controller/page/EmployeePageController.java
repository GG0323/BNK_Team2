package com.example.bnk.controller.page;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class EmployeePageController {
	
	@GetMapping("/toMain") // /employee/toMain
	public String mainWorkSpace() {
		return "Employees/mainWorkspaceLogin";
	}
	

	
	@GetMapping("/manager/HRM/hrmRegist")  // /employee/manager/HRM/hrmRegist
	public String hrmRegist() {
		return "Employees/manager/HRM/hrmRegist";
	}
	
	
	
	
}
