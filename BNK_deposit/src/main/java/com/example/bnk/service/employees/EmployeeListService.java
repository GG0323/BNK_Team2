package com.example.bnk.service.employees;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.employee.IEmployeeDao;
import com.example.bnk.dto.employee.EmployeeDetailUpdateDto;
import com.example.bnk.dto.employee.EmployeeListDetailDto;
import com.example.bnk.dto.employee.EmployeeListDto;

@Service
public class EmployeeListService {
	
	@Autowired
	private IEmployeeDao empDao;
	
	
	
	// 사원 전체 리스트 , 부서이름 출력
	public List<EmployeeListDto> allList(){
		
		List<EmployeeListDto> allList = empDao.employeeAllList();
		
		return allList;
	}
	// 사원 리스트 부서별 검색__>>
	
	// 사원 상세 페이지
	public EmployeeListDetailDto detail(
			long employee_no
			){
		
		EmployeeListDetailDto detail = empDao.employeeDetail(employee_no);
		
		return detail;
	}
	
	// 사원 정보 업데이트
	public int updateEmployeeDetail(
			EmployeeDetailUpdateDto detailDto
			) {
		int result = empDao.updateEmployeeDetail(detailDto);
		return result;
	}
	
	
	
	
}
