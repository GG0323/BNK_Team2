package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.BankMemberDto;

@Mapper
public interface IBankMemberDao {
	
	// 회원 로그인 시 기록
	public void lastLogin(long pk);
	
	// 회원 id로 조회
	public BankMemberDto findByUsername(@Param("id") String id);
	
	// 회원 등록
	public int regist(@Param("dto") BankMemberDto dto);
	
	// 회원 정보 수정
	public int editMember(@Param("dto") BankMemberDto dto);
	
	// 회원 이름, 전화번호, 생일로 조회
	public List<BankMemberDto> showMember(
			@Param("birth_date") String brith_date, 
			@Param("phone_number") String phone_number, 
			@Param("member_name") String member_name
		);
	
	// 회원 전체 조회
	public List<BankMemberDto> findByAll();
	
	// 회원 id로 전체 내용 조회
	public BankMemberDto findMemberById(@Param("loginId") String loginId);
	
	// 회원 비밀번호 수정
	public void updatePassword(@Param("loginId") String loginId, @Param("newPassword") String newPassword);
	
	// 회원 id 중복 확인
	public int idCheck(String id);

}
