package com.example.bnk.dao.community;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.community.CommunityAccountDto;

@Mapper
public interface ICommunityAccountDao {
	
	CommunityAccountDto searchMember(@Param("member_no") long member_no);
	CommunityAccountDto searchNickname(@Param("nickname")String nickname);
	int registComuAccount(CommunityAccountDto dto);
	int updateNickname(@Param("community_account_no") long communityAccountNo, @Param("nickname") String nickname);
}
