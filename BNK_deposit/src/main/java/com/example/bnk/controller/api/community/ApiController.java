package com.example.bnk.controller.api.community;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.auth.MemberDetails;
import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.community.CommunityAccountDto;
import com.example.bnk.dto.community.CommunityBoard;
import com.example.bnk.dto.community.CommunityReply;
import com.example.bnk.service.community.CommunityService;
import com.example.bnk.service.product.ProductSalesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class ApiController {

	private final CommunityService communityService;
	private final ProductSalesService productSalesService;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<?>> me(@AuthenticationPrincipal MemberDetails memberDetails) {
		return ResponseEntity.ok(ApiResponse.ok(toProfile(memberNo(memberDetails))));
	}

	@GetMapping("/check-login/{member_no}")
	public ResponseEntity<ApiResponse<?>> checkLogin(
			@PathVariable("member_no") long memberNo,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		long currentMemberNo = memberNo(memberDetails);
		if (memberNo != currentMemberNo) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ApiResponse.fail("다른 회원의 커뮤니티 정보를 조회할 수 없습니다."));
		}

		return ResponseEntity.ok(ApiResponse.ok(toProfile(currentMemberNo)));
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<?>> registerMember(
			@RequestBody Map<String, Object> request,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		try {
			String nickname = requiredText(request, "nickname");
			CommunityAccountDto account = communityService.register(memberNo(memberDetails), nickname);
			productSalesService.upTermscommunityRegist(account.getMember_no());
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("커뮤니티 가입이 완료되었습니다.", toProfile(account)));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
		}
	}

	@PutMapping("/me/nickname")
	public ResponseEntity<ApiResponse<?>> updateNickname(
			@RequestBody Map<String, Object> request,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		try {
			String nickname = requiredText(request, "nickname");
			CommunityAccountDto account = communityService.updateNickname(memberNo(memberDetails), nickname);
			return ResponseEntity.ok(ApiResponse.ok("닉네임이 수정되었습니다.", toProfile(account)));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
		}
	}

	@GetMapping("/boards")
	public ResponseEntity<ApiResponse<?>> boards(
			@RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
			@RequestParam(value = "keyword", required = false) String keyword
	) {
		return ResponseEntity.ok(ApiResponse.ok(communityService.selectBoards(sort, keyword)));
	}

	@PostMapping("/boards")
	public ResponseEntity<ApiResponse<?>> createBoard(
			@RequestBody Map<String, Object> request,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		try {
			CommunityBoard board = communityService.createBoard(
					memberNo(memberDetails),
					requiredText(request, "title"),
					requiredText(request, "content")
			);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("게시글이 등록되었습니다.", board));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
		}
	}

	@GetMapping("/boards/{boardNo}")
	public ResponseEntity<ApiResponse<?>> board(@PathVariable("boardNo") long boardNo) {
		try {
			return ResponseEntity.ok(ApiResponse.ok(communityService.selectBoard(boardNo, true)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
		}
	}

	@PostMapping("/boards/{boardNo}/like")
	public ResponseEntity<ApiResponse<?>> likeBoard(@PathVariable("boardNo") long boardNo) {
		try {
			return ResponseEntity.ok(ApiResponse.ok(communityService.likeBoard(boardNo)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
		}
	}

	@DeleteMapping("/boards/{boardNo}")
	public ResponseEntity<ApiResponse<?>> deleteBoard(
			@PathVariable("boardNo") long boardNo,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		try {
			communityService.deleteBoard(memberNo(memberDetails), boardNo);
			return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다."));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
		}
	}

	@GetMapping("/boards/{boardNo}/replies")
	public ResponseEntity<ApiResponse<?>> replies(@PathVariable("boardNo") long boardNo) {
		return ResponseEntity.ok(ApiResponse.ok(communityService.selectReplies(boardNo)));
	}

	@PostMapping("/boards/{boardNo}/replies")
	public ResponseEntity<ApiResponse<?>> createReply(
			@PathVariable("boardNo") long boardNo,
			@RequestBody Map<String, Object> request,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		try {
			CommunityReply reply = communityService.createReply(
					memberNo(memberDetails),
					boardNo,
					requiredText(request, "content")
			);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("댓글이 등록되었습니다.", reply));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
		}
	}

	@DeleteMapping("/replies/{replyNo}")
	public ResponseEntity<ApiResponse<?>> deleteReply(
			@PathVariable("replyNo") long replyNo,
			@AuthenticationPrincipal MemberDetails memberDetails
	) {
		try {
			communityService.deleteReply(memberNo(memberDetails), replyNo);
			return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
		}
	}

	private long memberNo(MemberDetails memberDetails) {
		if (memberDetails == null) {
			throw new IllegalStateException("로그인이 필요합니다.");
		}

		return memberDetails.getPk();
	}

	private Map<String, Object> toProfile(long memberNo) {
		return toProfile(communityService.selectMember(memberNo));
	}

	private Map<String, Object> toProfile(CommunityAccountDto account) {
		Map<String, Object> profile = new LinkedHashMap<>();
		boolean isMember = account != null && "ACTIVE".equals(account.getCommunity_status());
		profile.put("isMember", isMember);

		if (isMember) {
			profile.put("community_account_no", account.getCommunity_account_no());
			profile.put("member_no", account.getMember_no());
			profile.put("nickname", account.getNickname());
			profile.put("account_role", account.getAccount_role());
			profile.put("community_status", account.getCommunity_status());
			profile.put("created_at", account.getCreated_at());
		}

		return profile;
	}

	private String requiredText(Map<String, Object> request, String key) {
		Object value = request == null ? null : request.get(key);
		String text = value == null ? "" : value.toString().trim();

		if (text.isEmpty()) {
			throw new IllegalArgumentException(key + " 값이 필요합니다.");
		}

		return text;
	}
}
