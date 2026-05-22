package com.example.bnk.controller.api.member;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.AccountHistoryDto;
import com.example.bnk.dto.member.AccountTransactionDto;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.member.MemberProductDto;
import com.example.bnk.dto.member.MemberTrackingLogDto;
import com.example.bnk.dto.member.MypageSummaryDto;
import com.example.bnk.service.member.AccountService;
import com.example.bnk.service.member.AccountTransactionService;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.member.MemberTrackingLogService;
import com.example.bnk.service.product.ProductSalesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankMemberApiController {

	private final BankMemberService bankMemberService;
	private final AccountService accountService;
	private final ProductSalesService productSalesService;
	private final MemberTrackingLogService memberTrackingLogService;
	private final AccountTransactionService accountTransactionService;
	
	private BankMemberDto getLoginMember(Principal principal) {
	    if (principal == null) {
	        return null;
	    }

	    String currentLoginId = principal.getName();
	    return bankMemberService.getMemberInfo(currentLoginId);
	}

	// ===================== 조회(GET) =====================

	// 마이페이지 요약 데이터
	@GetMapping("/mypage")
	public ResponseEntity<ApiResponse<?>> getMypageSummary(Principal principal) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }

	    String currentLoginId = principal.getName();
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);

	    if (memberInfo == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
	    }

	    long currentMemberNo = memberInfo.getMember_no();

	    List<AccountDto> accountList = accountService.getAccounts(currentMemberNo);
	    int accountCount = accountList.size();
	    long totalBalance = accountList.stream().mapToLong(AccountDto::getBalance).sum();

	    int logCount = memberTrackingLogService.getLogCount(currentMemberNo);
	    int productCount = productSalesService.getSubscribedProductCount(currentMemberNo);
	    List<MemberTrackingLogDto> recentLogs = memberTrackingLogService.getRecentLogs(currentMemberNo);

	    MypageSummaryDto summary = MypageSummaryDto.builder()
	            .member(memberInfo)
	            .accountCount(accountCount)
	            .totalBalance(totalBalance)
	            .productCount(productCount)
	            .logCount(logCount)
	            .recentLogs(recentLogs)
	            .build();

	    return ResponseEntity.ok(ApiResponse.ok(summary));
	}

	// 내 정보 조회
	@GetMapping("/myinfo")
	public ResponseEntity<ApiResponse<?>> getMyInfo(Principal principal) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }

	    String currentLoginId = principal.getName();
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);

	    if (memberInfo == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
	    }

	    return ResponseEntity.ok(ApiResponse.ok(memberInfo));
	}

	// 내 계좌 목록 조회
	@GetMapping("/myaccounts")
	public ResponseEntity<ApiResponse<?>> getMyAccounts(Principal principal) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }

	    String currentLoginId = principal.getName();
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);

	    if (memberInfo == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
	    }

	    long currentMemberNo = memberInfo.getMember_no();
	    List<AccountDto> accountList = accountService.getAccounts(currentMemberNo);

	    return ResponseEntity.ok(ApiResponse.ok(accountList));
	}

	// 계좌 상세 + 거래내역 조회
	@GetMapping("/accounts/{accountNo}/history")
	public ResponseEntity<ApiResponse<?>> getAccountHistory(
	        Principal principal,
	        @PathVariable(name = "accountNo") Long accountNo) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }

	    String currentLoginId = principal.getName();
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);

	    if (memberInfo == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
	    }

	    AccountDto account = accountService.getAccountDetail(accountNo);

	    if (account == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ApiResponse.fail("계좌 정보를 찾을 수 없습니다."));
	    }

	    if (account.getMember_no() != memberInfo.getMember_no()) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ApiResponse.fail("본인 계좌만 조회할 수 있습니다."));
	    }

	    List<AccountTransactionDto> transactionList =
	            accountTransactionService.getTransactions(accountNo);

	    AccountHistoryDto historyData = AccountHistoryDto.builder()
	            .account(account)
	            .transactionList(transactionList)
	            .build();

	    return ResponseEntity.ok(ApiResponse.ok(historyData));
	}

	// 가입 상품 내역 조회
	@GetMapping("/myproducts")
	public ResponseEntity<ApiResponse<?>> getMyProducts(Principal principal) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }

	    String currentLoginId = principal.getName();
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);

	    if (memberInfo == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
	    }

	    long currentMemberNo = memberInfo.getMember_no();
	    List<MemberProductDto> productList = productSalesService.getSubscribedProducts(currentMemberNo);

	    return ResponseEntity.ok(ApiResponse.ok(productList));
	}

	// 내 정보 수정
	@PostMapping("/myinfo/update")
	public ResponseEntity<ApiResponse<Void>> updateMyInfo(
	        Principal principal,
	        @RequestParam(value = "phone_number", defaultValue = "") String phoneNumber,
	        @RequestParam(value = "email", defaultValue = "") String email,
	        @RequestParam(value = "address_main", defaultValue = "") String addressMain,
	        @RequestParam(value = "address_detail", defaultValue = "") String addressDetail) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }
		String currentLoginId = principal.getName();

		// 입력 데이터가 아예 없으면 DB 접근 차단 (Early Return)
		if (phoneNumber.trim().isEmpty() && email.trim().isEmpty() && addressMain.trim().isEmpty()) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.fail("수정할 정보가 입력되지 않았습니다."));
		}

		// 전화번호 백엔드 검증
		if (!phoneNumber.matches("^010-\\d{4}-\\d{4}$")) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.fail("전화번호 형식이 올바르지 않거나 조작되었습니다."));
		}

		// 이메일 백엔드 검증 (비어있지 않은 경우에만 검증)
		if (!email.trim().isEmpty() && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.fail("이메일 형식이 올바르지 않거나 조작되었습니다."));
		}

		// editMember 매퍼는 <if test="dto.xxx != null"> 방식이라 null 인 필드는 건너뛴다.
		// 따라서 빈 값은 setter 를 호출하지 않아(= null 유지) 기존 DB 값을 보존하고,
		// 값이 있는 항목만 갱신되도록 한다.
		BankMemberDto updateDto = new BankMemberDto();
		updateDto.setLogin_id(currentLoginId); // WHERE 조건용 (필수)

		// 전화번호: 위에서 정규식 검증을 통과했으므로 항상 유효값
		updateDto.setPhone_number(phoneNumber);

		// 이메일: 입력이 있을 때만 갱신
		if (!email.trim().isEmpty()) {
			updateDto.setEmail(email);
		}

		// 주소: 입력이 있을 때만 (상세주소가 있으면 합쳐서) 갱신
		if (!addressMain.trim().isEmpty()) {
			String fullAddress = addressMain;
			if (addressDetail != null && !addressDetail.trim().isEmpty()) {
				fullAddress += " " + addressDetail;
			}
			updateDto.setAdress(fullAddress);
		}

		bankMemberService.modifyMemberInfo(updateDto);

		return ResponseEntity.ok(ApiResponse.success("개인정보가 성공적으로 수정되었습니다."));
	}

	// 비밀번호 변경
	@PostMapping("/myinfo/update-password")
	public ResponseEntity<ApiResponse<Void>> updatePassword(
	        Principal principal,
	        @RequestParam(value = "current_password", defaultValue = "") String currentPassword,
	        @RequestParam(value = "new_password", defaultValue = "") String newPassword) {

	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ApiResponse.fail("로그인이 필요합니다."));
	    }
		String currentLoginId = principal.getName();

		if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.fail("비밀번호를 정확히 입력해주세요."));
		}

		boolean isChanged = bankMemberService.changePassword(currentLoginId, currentPassword, newPassword);

		if (isChanged) {
			return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.fail("현재 비밀번호가 일치하지 않습니다."));
		}
	}
}