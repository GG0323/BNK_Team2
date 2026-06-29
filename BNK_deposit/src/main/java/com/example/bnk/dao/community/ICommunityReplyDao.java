package com.example.bnk.dao.community;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.community.CommunityReply;

@Mapper
public interface ICommunityReplyDao {
	List<CommunityReply> selectReplies(@Param("board_no") long boardNo);
	CommunityReply selectLatestReplyByAccount(
			@Param("board_no") long boardNo,
			@Param("community_account_no") long communityAccountNo
	);
	int insertReply(CommunityReply reply);
	int deleteReply(@Param("reply_no") long replyNo, @Param("community_account_no") long communityAccountNo);
}
