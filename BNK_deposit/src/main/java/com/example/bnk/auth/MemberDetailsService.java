package com.example.bnk.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dto.member.BankMemberDto;

@Service
public class MemberDetailsService implements UserDetailsService{
	
	@Autowired
	private IBankMemberDao dao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println(username);
		BankMemberDto dto = dao.findByUsername(username);
		if(dto == null)	 throw new UsernameNotFoundException("해당 직원을 찾을 수 없습니다: " + username);
		return new MemberDetails(dto);
	}

}
