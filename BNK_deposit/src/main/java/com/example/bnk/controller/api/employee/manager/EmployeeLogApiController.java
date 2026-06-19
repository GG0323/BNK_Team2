package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.auth.EmployeeDetails;
import com.example.bnk.dto.employee.EmployeeLogDto;
import com.example.bnk.dto.employee.EmployeeLogSelectDto;
import com.example.bnk.service.employees.EmployeeLogService;

@RestController
@RequestMapping("/api/log/employee")
public class EmployeeLogApiController {
	
	@Autowired
	private EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.");

	
	
	// 사원 활동 이력 
	@GetMapping("/allList")
	public List<EmployeeLogDto> allList(
			@AuthenticationPrincipal EmployeeDetails employeeD
			){
		logService.build(employeeD.getUsername(), "SELECT", "TB_EMPLOYEE_LOG", "*", " 사원 활동기록을 조회한다. ");
		
		List<EmployeeLogDto> list  = logService.allLog();
		
		return list;
	}
	
	// 사원 활동이력 조건 검색
	@GetMapping("/conditionList")
	public List<EmployeeLogDto> conditionList(
			EmployeeLogSelectDto selectDto ,
			@AuthenticationPrincipal EmployeeDetails employeeD
			){
		System.out.println(selectDto.toString());
		
		String selectKey =
			    "empNo=" + selectDto.getEmployee_no()
			    + ", action=" + selectDto.getAction_type()
			    + ", table=" + selectDto.getTarget_table();
		//로그
		logService.build(employeeD.getUsername(), "SELECT", "TB_EMPLOYEE_LOG", selectKey , " 사원 활동기록을 조회한다. ");
		
		List<EmployeeLogDto> list  = logService.conditionList(selectDto);
		
		return list;
	}
	
	
	
}
