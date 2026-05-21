package com.example.bnk.controller.api.employee.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.service.employees.EmployeeLogService;

@RestController
@RequestMapping("/api/employeeList")
public class EmployeeListApiController {
	
	@Autowired
	private EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	
	
	
	
	
	
}
