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
		EmployeeDto dto = dao.findByUsername(username);
		if(dto == null) throw new UsernameNotFoundException("해당 직원을 찾을 수 없습니다: " + username);
		return new EmployeeDetails(dto);
	}

}
