package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	public List<EmployeeListDto> allList(){
		//logService.build("SELECT", "TB_EMPLOYEE", null, "사원 목록을 출력한다. ", "GET", "/api/employeeList/allList");
		
		List<EmployeeListDto> allList = listService.allList();
		
		return allList;
	}
	// 부서별로 검색__>>
	
	
	// 직원 pk로 상세 페이지 검색
	@GetMapping("/detail")
	public EmployeeListDetailDto detailDto(
			@RequestParam("employee_no")long employee_no
			) {
		System.out.println("사원 pk는 " + employee_no);
		
		EmployeeListDetailDto detail = listService.detail(employee_no);
		System.out.println("이미지 url : "+detail.getImg_url());
		
		return detail;
	}
	
	// 직원 상세정보 수정 
	@PostMapping("/updateEmployeeDetale")
	public void updateEmployeeDetale(
			EmployeeListDetailDto detailDto
			) {
		System.out.println("수정 정보 확인 "+detailDto.toString());
	}
	
	
	
}
