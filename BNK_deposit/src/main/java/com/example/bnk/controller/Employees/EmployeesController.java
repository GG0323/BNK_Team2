package com.example.bnk.controller.Employees;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/Employees")
public class EmployeesController {
	
	@GetMapping("/toMain")
	public String mainWorkSpace() {
		return "Employees/mainWorkspace";
	}
	
	
	
}
