package com.example.bnk.service.community;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.community.ICommunityAccountDao;
import com.example.bnk.dto.community.CommunityAccountDto;


@Service
public class CommunityService {

	@Autowired
	public ICommunityAccountDao iCommunityAccountDao;
	
	// 이미 가입한 사람인지 확인
	public int searchMember(long member_no) {
		CommunityAccountDto result = iCommunityAccountDao.searchMember(member_no);
		if(result == null) {
			System.out.println("조회된 회원이 없습니다.");
			return 1;
		}
		System.out.println(result);
		return 0;
	}
	
	// 이미 가입한 사람의 데이터 받기
	public CommunityAccountDto selectMember(long member_no) {
		CommunityAccountDto result = iCommunityAccountDao.searchMember(member_no);
		if(result == null) {
			System.out.println("회원 데이터가 없습니다.");
			return null;
		}
		System.out.println(result);
		return result;
	}
	
	
	// 닉네임 중복 확인하기
	public int searchNickname(String nickname) {
		CommunityAccountDto result = iCommunityAccountDao.searchNickname(nickname);
		if(result == null) {
			System.out.println("사용할 수 있는 닉네임입니다.");
			return 1;
		}
		System.out.println("사용할 수 없는 닉네임입니다.");
		return 0;
	}
	
	// 회원가입 하기
	public int registComuAccount(CommunityAccountDto dto) {
		
		if(iCommunityAccountDao.registComuAccount(dto) == 0) {
			System.out.println("회원가입에 실패하였습니다.");
			return 0;
		}
		System.out.println("회원가입에 성공하셨습니다.");
		return 1;
	}
}