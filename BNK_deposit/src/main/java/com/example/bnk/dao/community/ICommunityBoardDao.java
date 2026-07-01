package com.example.bnk.dao.community;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.community.CommunityBoard;

@Mapper
public interface ICommunityBoardDao {
	List<CommunityBoard> selectBoards(@Param("sort") String sort, @Param("keyword") String keyword);
	CommunityBoard selectBoard(@Param("board_no") long boardNo);
	CommunityBoard selectLatestBoardByAccount(@Param("community_account_no") long communityAccountNo);
	int insertBoard(CommunityBoard board);
	int updateBoard(
			@Param("board_no") long boardNo,
			@Param("community_account_no") long communityAccountNo,
			@Param("board_title") String title,
			@Param("board_content") String content
	);
	int updateViewCount(@Param("board_no") long boardNo);
	int updateLikeCount(@Param("board_no") long boardNo);
	int deleteBoard(@Param("board_no") long boardNo, @Param("community_account_no") long communityAccountNo);
}
