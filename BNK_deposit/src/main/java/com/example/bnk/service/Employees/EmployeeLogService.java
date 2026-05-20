package com.example.bnk.service.Employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.employee.IEmployeeLogDao;
import com.example.bnk.dto.employee.EmployeeLogInsertDto;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class EmployeeLogService {
	
	@Autowired
	private HttpServletRequest request; // 스프링이 현재 요청의 객체를 주입한다.
	@Autowired
	private IEmployeeLogDao logDao;
	
	public void log(EmployeeLogInsertDto insertDto) {
		
		
		int result = logDao.insertLog(insertDto);
		
		if(result == 1) System.out.println("로그 저장 성공 ");
		
		
	}
	
	public EmployeeLogInsertDto build(
			String action_type, String target_table, String target_pk, 
			String action_detail,String request_method, String request_url 
			) {
		EmployeeLogInsertDto insertDto = new EmployeeLogInsertDto(
				action_type, target_table, target_pk,
				action_detail, request_method, request_url);
		
		request.setAttribute("insertLogDto", insertDto);
		
		return insertDto;
	}
	
	
	
	
	
	
}
