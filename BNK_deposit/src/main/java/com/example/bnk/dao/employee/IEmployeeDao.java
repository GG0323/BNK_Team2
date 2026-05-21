package com.example.bnk.dao.employee;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.employee.EmployeeRegistInsertDto;

@Mapper
public interface IEmployeeDao {
	
	// 사원 전체 조회
	public List<EmployeeDto> showAllEmp();
	
	// 사원 1명 조회
	public EmployeeDto findByUsername(String username);
	
	// 사원 id 검사
	public EmployeeDto login(String login_id);
	
	// 사원 등록
	public int regist(EmployeeRegistInsertDto insertDto);
	
}
