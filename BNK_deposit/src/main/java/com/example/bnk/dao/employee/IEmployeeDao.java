package com.example.bnk.dao.employee;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeLoginDto;

@Mapper
public interface IEmployeeDao {
	
	// 멤버 id 검사
	public EmployeeLoginDto login(String login_id);
	
	
	
}
