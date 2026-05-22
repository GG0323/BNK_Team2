package com.example.bnk.dao.employee;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.employee.EmployeeDetailUpdateDto;
import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.employee.EmployeeListDetailDto;
import com.example.bnk.dto.employee.EmployeeListDto;
import com.example.bnk.dto.employee.EmployeeRegistInsertDto;

@Mapper
public interface IEmployeeDao {
	
	// 사원 전체 조회
	public List<EmployeeDto> showAllEmp();
	
	// 전체 사원 + 부서이름 
	public List<EmployeeListDto> employeeAllList();
	
	// 사원 상세 페이지
	public EmployeeListDetailDto employeeDetail(long employee_no);
	
	
	// 사원 1명 조회
	public EmployeeDto findByUsername(String username);
	
	// 사원 id 검사
	public EmployeeDto login(String login_id);
	
	
	// 사원 등록
	public int regist(@Param("dto") EmployeeRegistInsertDto insertDto);
	// 사원 정보 수정
	public int updateEmployeeDetail(@Param("dto") EmployeeDetailUpdateDto detailDto);
	
}
