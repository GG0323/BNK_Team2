package com.example.bnk.dao.employee;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.employee.EmployeeRegistInsertDto;

@Mapper
public interface IEmployeeDao {
	
	public List<EmployeeDto> showAllEmp();
	
	// 멤버 id 검사
	public EmployeeDto login(String login_id);
	
	// 사원 등록
	public int regist(EmployeeRegistInsertDto insertDto);
	
}
