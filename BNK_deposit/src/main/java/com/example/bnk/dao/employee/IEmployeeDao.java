package com.example.bnk.dao.employee;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeDto;

@Mapper
public interface IEmployeeDao {
	
	// 멤버 id 검사
	public EmployeeDto login(String login_id);
	
	
	
}
