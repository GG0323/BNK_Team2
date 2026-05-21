package com.example.bnk.dao.employee;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeLogDto;
import com.example.bnk.dto.employee.EmployeeLogInsertDto;
import com.example.bnk.dto.employee.EmployeeLogSelectDto;

@Mapper
public interface IEmployeeLogDao {
	
	// 직원 활동로그 인서트
	public int insertLog(EmployeeLogInsertDto insertDto);
	
	
	// 전체 로그 조회
	public List<EmployeeLogDto> allLog();
	
	// 조건 검색 로그 조회 > 동적 쿼리
	public List<EmployeeLogDto> conditionLog(EmployeeLogSelectDto selectDto);
	
}
