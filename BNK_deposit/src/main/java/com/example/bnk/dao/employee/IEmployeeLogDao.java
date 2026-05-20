package com.example.bnk.dao.employee;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeLogInsertDto;

@Mapper
public interface IEmployeeLogDao {
	
	// 직원 활동로그 인서트
	public int insertLog(EmployeeLogInsertDto insertDto);
	
	
	// 전체 로그 > 동적 쿼리
	
	
}
