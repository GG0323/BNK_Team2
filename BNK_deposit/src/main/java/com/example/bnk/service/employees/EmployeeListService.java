package com.example.bnk.service.employees;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.employee.IEmployeeDao;
import com.example.bnk.dto.employee.EmployeeDetailUpdateDto;
import com.example.bnk.dto.employee.EmployeeDto;
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
	
	
	
	// 상품제안서 작성을 위한 상사들의 목록을 반환하는 함수
	public List<EmployeeDto> managers() {
		List<EmployeeDto> managers = empDao.managers();
		return managers;
	}
	// 로그인한 사원의 정보를 auth영역에서 로그인 id를 꺼내와서 DB에 검색한다. 
	public EmployeeDto findByUsername(String username) {
		EmployeeDto myInfo = empDao.findByUsername(username);
		return myInfo;
	}
	
	
	
	
}
