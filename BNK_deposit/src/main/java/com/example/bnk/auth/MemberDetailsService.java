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
		BankMemberDto dto = dao.findByUsername(username);
		return dto != null ? new MemberDetails(dto) : null;
	}

}
