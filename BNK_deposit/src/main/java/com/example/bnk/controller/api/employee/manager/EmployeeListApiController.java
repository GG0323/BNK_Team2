package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.auth.EmployeeDetails;
import com.example.bnk.dto.employee.EmployeeDetailUpdateDto;
import com.example.bnk.dto.employee.EmployeeListDetailDto;
import com.example.bnk.dto.employee.EmployeeListDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.employees.EmployeeLogService;

@RestController
@RequestMapping("/api/employeeList")
public class EmployeeListApiController {
	
	@Autowired
	private EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	@Autowired
	private EmployeeListService listService;
	
	
	// 직원 리스트 불ㄹ러오기
	@GetMapping("/allList")
	public List<EmployeeListDto> allList(
			@AuthenticationPrincipal EmployeeDetails employeeD
			){
		
		logService.build(employeeD.getUsername(), "SELECT", "TB_EMPLOYEE", null, "전체 사원 목록을 출력한다. ");
		
		List<EmployeeListDto> allList = listService.allList();
		
		return allList;
	}
	// 부서별로 검색__>>
	
	
	// 직원 pk로 상세 페이지 검색
	@GetMapping("/detail")
	public EmployeeListDetailDto detailDto(
			@RequestParam("employee_no")long employee_no,
			@AuthenticationPrincipal EmployeeDetails employeeD
			) {
		System.out.println("사원 pk는 " + employee_no);
		//로그
		logService.build(employeeD.getUsername(), "SELECT", "TB_EMPLOYEE", "사원 pk : " + employee_no, " 사원 상세를 출력한다. ");
		
		//서비스
		EmployeeListDetailDto detail = listService.detail(employee_no);
		System.out.println("이미지 url : "+detail.getImg_url());
		
		return detail;
	}
	
	// 직원 상세정보 수정 
	@PostMapping("/updateEmployeeDetale")
	public EmployeeListDetailDto updateEmployeeDetale(
			EmployeeDetailUpdateDto detailDto,
			@AuthenticationPrincipal EmployeeDetails employeeD
			) {
		System.out.println("수정 정보 확인 "+detailDto.toString());
		//로그
		logService.build(employeeD.getUsername(), "UPDATE", "TB_EMPLOYEE", detailDto.getEmployee_no()+"."+detailDto.getEmployee_name(), "사원 정보를 수정한다. ");
		
		// 업데이트 서비스 호출
		int result = listService.updateEmployeeDetail(detailDto);
		
		if(result == 1) System.out.println("업데이트 성공");
		
		// 디테일 서비스
		EmployeeListDetailDto detail = listService.detail(detailDto.getEmployee_no());
		
		return detail;
	}
	
	
	
}
