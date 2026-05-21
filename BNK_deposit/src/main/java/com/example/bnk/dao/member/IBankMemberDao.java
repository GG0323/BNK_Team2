package com.example.bnk.dao.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.BankMemberDto;

@Mapper
public interface IBankMemberDao {
	public BankMemberDto findByUsername(@Param("id") String id);
	public int regist(@Param("dto") BankMemberDto dto);
	public int editMember(@Param("dto") BankMemberDto dto);
}
