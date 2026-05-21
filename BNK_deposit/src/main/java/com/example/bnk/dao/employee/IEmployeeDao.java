package com.example.bnk.dao.employee;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.employee.EmployeeDto;

@Mapper
public interface IEmployeeDao {
	public List<EmployeeDto> showAllEmp();
}
