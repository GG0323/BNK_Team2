package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.BankMemberDto;

@Mapper
public interface IBankMemberDao {

	public List<BankMemberDto> showMember(
			@Param("birth_date") String brith_date, 
			@Param("phone_number") String phone_number, 
			@Param("member_name") String member_name
		);

	BankMemberDto findMemberById(String loginId);

	void updateMemberInfo(BankMemberDto updateDto);

	void updatePassword(@Param("loginId") String loginId, @Param("newPassword") String newPassword);
}
