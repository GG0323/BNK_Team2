package com.example.bnk.service.employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.employee.IEmployeeDao;
import com.example.bnk.dto.employee.EmployeeDto;

@Service
public class EmployeesLoginService {
	
	@Autowired
	private IEmployeeDao iEmpDao;
	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	//로그인 서비스
	public boolean login(String login_id, String password) {
		System.out.println("서비스 로그인 파리미터 확인"+login_id);
		
		//문제 지점
		EmployeeDto empDto = iEmpDao.login(login_id);
		
		if(empDto == null) {
			return false;
		}else {
			String dbpw = empDto.getPassword_hash();
			
			String input = passwordEncode.encode(password);
			
			if(!input.equals(dbpw)) {
				return false;
			}
			
			return true;	
		}
	}
	
	
	
	// 회원 등록 을 먼저 해야한다.....
	
	
	
	
	
}


