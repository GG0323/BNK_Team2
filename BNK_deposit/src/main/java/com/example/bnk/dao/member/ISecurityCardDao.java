package com.example.bnk.dao.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.SecurityCardDto;

@Mapper
public interface ISecurityCardDao {
	int insertSecurityCard(SecurityCardDto dto);

	SecurityCardDto findActiveByMemberNo(@Param("memberNo") long memberNo);
}
