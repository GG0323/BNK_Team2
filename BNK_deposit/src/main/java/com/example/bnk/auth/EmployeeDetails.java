package com.example.bnk.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.bnk.dto.employee.EmployeeDto;

public class EmployeeDetails implements UserDetails{
	
	private EmployeeDto employee;
	
	public EmployeeDetails(EmployeeDto employee) {
		this.employee = employee;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();
		collection.add(new GrantedAuthority() {
			
			@Override
			public @Nullable String getAuthority() {
				return employee.getEmployee_role();
			}
		});
		return collection;
	}

	@Override
	public @Nullable String getPassword() {
		return this.employee.getPassword_hash();
	}

	@Override
	public String getUsername() {
		return this.employee.getLogin_id();
	}
	
	public long getPk() {
		return this.employee.getEmployee_no();
	}

}
