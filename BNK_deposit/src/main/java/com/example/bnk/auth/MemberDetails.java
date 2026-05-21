package com.example.bnk.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.bnk.dto.member.BankMemberDto;

public class MemberDetails implements UserDetails{
	
	private BankMemberDto member;
	
	public MemberDetails(BankMemberDto member) {
		this.member = member;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();
		
		collection.add(new GrantedAuthority() {
			
			@Override
			public @Nullable String getAuthority() {
				return "MEMBER";
			}
		});
		
		return collection;
	}

	@Override
	public @Nullable String getPassword() {
		return this.member.getPassword_hash();
	}

	@Override
	public String getUsername() {
		return this.member.getLogin_id();
	}
	
	public long getPk() {
		return this.member.getMember_no();
	}

}
