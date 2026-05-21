package com.example.bnk.service.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dto.member.BankMemberDto;

@Service
public class MemberService {

	@Autowired
	private IBankMemberDao memberDao;
	
	// 회원 등록
	public boolean regist(BankMemberDto dto) {
		return memberDao.regist(dto) == 1;
	}
	
	// 회원 수정
	public boolean editMember(BankMemberDto dto) {
		return memberDao.editMember(dto) == 1;
	}
}
