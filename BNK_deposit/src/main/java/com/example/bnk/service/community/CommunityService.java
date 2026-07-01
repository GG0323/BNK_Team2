package com.example.bnk.service.community;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.community.ICommunityAccountDao;
import com.example.bnk.dao.community.ICommunityBoardDao;
import com.example.bnk.dao.community.ICommunityReplyDao;
import com.example.bnk.dto.community.CommunityAccountDto;
import com.example.bnk.dto.community.CommunityBoard;
import com.example.bnk.dto.community.CommunityReply;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

	private final ICommunityAccountDao communityAccountDao;
	private final ICommunityBoardDao communityBoardDao;
	private final ICommunityReplyDao communityReplyDao;

	public CommunityAccountDto selectMember(long memberNo) {
		return communityAccountDao.searchMember(memberNo);
	}

	public boolean isAvailableMember(long memberNo) {
		return communityAccountDao.searchMember(memberNo) == null;
	}

	public boolean isAvailableNickname(String nickname) {
		return communityAccountDao.searchNickname(nickname) == null;
	}

	@Transactional
	public CommunityAccountDto register(long memberNo, String nickname) {
		if (!isAvailableMember(memberNo)) {
			throw new IllegalArgumentException("이미 가입된 회원입니다.");
		}

		if (!isAvailableNickname(nickname)) {
			throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
		}

		CommunityAccountDto dto = new CommunityAccountDto();
		dto.setMember_no(memberNo);
		dto.setNickname(nickname);
		dto.setAccount_role("MEMBER");
		dto.setCommunity_status("ACTIVE");

		int inserted = communityAccountDao.registComuAccount(dto);
		if (inserted != 1) {
			throw new IllegalStateException("커뮤니티 가입에 실패했습니다.");
		}

		return communityAccountDao.searchMember(memberNo);
	}

	@Transactional
	public CommunityAccountDto updateNickname(long memberNo, String nickname) {
		CommunityAccountDto account = requireActiveAccount(memberNo);

		CommunityAccountDto duplicated = communityAccountDao.searchNickname(nickname);
		if (duplicated != null && duplicated.getCommunity_account_no() != account.getCommunity_account_no()) {
			throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
		}

		int updated = communityAccountDao.updateNickname(account.getCommunity_account_no(), nickname);
		if (updated != 1) {
			throw new IllegalStateException("닉네임 수정에 실패했습니다.");
		}

		return communityAccountDao.searchMember(memberNo);
	}

	public List<CommunityBoard> selectBoards(String sort, String keyword) {
		return communityBoardDao.selectBoards(normalizeSort(sort), normalizeKeyword(keyword));
	}

	@Transactional
	public CommunityBoard selectBoard(long boardNo, boolean increaseViewCount) {
		if (increaseViewCount) {
			communityBoardDao.updateViewCount(boardNo);
		}

		CommunityBoard board = communityBoardDao.selectBoard(boardNo);
		if (board == null) {
			throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
		}

		return board;
	}

	@Transactional
	public CommunityBoard createBoard(long memberNo, String title, String content) {
		CommunityAccountDto account = requireActiveAccount(memberNo);

		CommunityBoard board = new CommunityBoard();
		board.setCommunity_account_no(account.getCommunity_account_no());
		board.setBoard_title(title);
		board.setBoard_content(content);

		int inserted = communityBoardDao.insertBoard(board);
		if (inserted != 1) {
			throw new IllegalStateException("게시글 등록에 실패했습니다.");
		}

		return communityBoardDao.selectLatestBoardByAccount(account.getCommunity_account_no());
	}

	@Transactional
	public CommunityBoard likeBoard(long boardNo) {
		int updated = communityBoardDao.updateLikeCount(boardNo);
		if (updated != 1) {
			throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
		}

		return communityBoardDao.selectBoard(boardNo);
	}

	@Transactional
	public CommunityBoard updateBoard(long memberNo, long boardNo, String title, String content) {
		CommunityAccountDto account = requireActiveAccount(memberNo);
		int updated = communityBoardDao.updateBoard(
				boardNo,
				account.getCommunity_account_no(),
				title,
				content
		);
		if (updated != 1) {
			throw new IllegalArgumentException("?섏젙??寃뚯떆湲??李얠쓣 ???놁뒿?덈떎.");
		}

		return communityBoardDao.selectBoard(boardNo);
	}

	@Transactional
	public void deleteBoard(long memberNo, long boardNo) {
		CommunityAccountDto account = requireActiveAccount(memberNo);
		int updated = communityBoardDao.deleteBoard(boardNo, account.getCommunity_account_no());
		if (updated != 1) {
			throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다.");
		}
	}

	public List<CommunityReply> selectReplies(long boardNo) {
		return communityReplyDao.selectReplies(boardNo);
	}

	@Transactional
	public CommunityReply createReply(long memberNo, long boardNo, String content) {
		selectBoard(boardNo, false);
		CommunityAccountDto account = requireActiveAccount(memberNo);

		CommunityReply reply = new CommunityReply();
		reply.setBoard_no(boardNo);
		reply.setCommunity_account_no(account.getCommunity_account_no());
		reply.setReply_content(content);

		int inserted = communityReplyDao.insertReply(reply);
		if (inserted != 1) {
			throw new IllegalStateException("댓글 등록에 실패했습니다.");
		}

		return communityReplyDao.selectLatestReplyByAccount(boardNo, account.getCommunity_account_no());
	}

	@Transactional
	public CommunityReply updateReply(long memberNo, long replyNo, String content) {
		CommunityAccountDto account = requireActiveAccount(memberNo);
		int updated = communityReplyDao.updateReply(replyNo, account.getCommunity_account_no(), content);
		if (updated != 1) {
			throw new IllegalArgumentException("?섏젙???볤???李얠쓣 ???놁뒿?덈떎.");
		}

		return communityReplyDao.selectReply(replyNo);
	}

	@Transactional
	public void deleteReply(long memberNo, long replyNo) {
		CommunityAccountDto account = requireActiveAccount(memberNo);
		int updated = communityReplyDao.deleteReply(replyNo, account.getCommunity_account_no());
		if (updated != 1) {
			throw new IllegalArgumentException("삭제할 댓글을 찾을 수 없습니다.");
		}
	}

	private CommunityAccountDto requireActiveAccount(long memberNo) {
		CommunityAccountDto account = communityAccountDao.searchMember(memberNo);

		if (account == null || !"ACTIVE".equals(account.getCommunity_status())) {
			throw new IllegalStateException("커뮤니티 가입이 필요합니다.");
		}

		return account;
	}

	private String normalizeSort(String sort) {
		if ("likes".equals(sort) || "oldest".equals(sort)) {
			return sort;
		}

		return "latest";
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return null;
		}

		return keyword.trim();
	}
}
