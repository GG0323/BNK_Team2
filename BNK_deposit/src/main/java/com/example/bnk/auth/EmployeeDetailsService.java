package com.example.bnk.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.employee.IEmployeeDao;
import com.example.bnk.dto.employee.EmployeeDto;

@Service
public class EmployeeDetailsService implements UserDetailsService{
	
	@Autowired
	private IEmployeeDao dao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		System.out.println("시큐리티~~~");
		
		EmployeeDto dto = dao.findByUsername(username);
		System.out.println("시큐리티 로그인~~ "+dto.toString());
		
		return dto != null ? new EmployeeDetails(dto) : null;
	}

}
