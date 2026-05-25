package com.example.bnk.controller.api.employee.manager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/approved")
public class ApprovedApiController {
	
	
	
	
	@GetMapping("/approvedList")
	public void approvedList() {
		//리턴할 DTO 있어야하고 
		//서비스 있어야하고
		//
	}
	
	

}
