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

/**
 * 회원 마이페이지 관련 "데이터 처리"를 전담하는 REST API 컨트롤러.
 *
 * - 화면에 표시할 모든 조회 데이터(GET)와 변경 요청(POST)을 JSON 으로 처리한다.
 * - Page Controller 는 빈 템플릿만 반환하고, 실제 데이터는 이 API 가 책임진다.
 * - 응답은 ApiResponse 표준 포맷으로 감싸 웹/앱이 동일하게 파싱하도록 한다.
 *
 * 식별번호 마스킹:
 *   BankMemberService.getMemberInfo() 가 AES 복호화 + 마스킹(개인/기업 구분)을
 *   이미 수행하므로, 컨트롤러에서는 별도 마스킹을 하지 않고 결과를 그대로 내려준다.
 *
 * 인증: 현재는 세션 기반 Principal 을 사용한다.
 *       추후 모바일 앱을 위해 JWT 로 전환할 경우, 로그인 ID 를 꺼내는 부분만
 *       (principal.getName()) 토큰에서 추출하도록 교체하면 나머지는 그대로 재사용 가능하다.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankMemberApiController {

	private final BankMemberService bankMemberService;
	private final AccountService accountService;
	private final ProductSalesService productSalesService;
	private final MemberTrackingLogService memberTrackingLogService;
	private final AccountTransactionService accountTransactionService;

	// ===================== 조회(GET) =====================

	// 마이페이지 요약 데이터 (회원 + 계좌수 + 총잔액 + 상품수 + 접속기록)
	@GetMapping("/mypage")
	public ResponseEntity<ApiResponse<MypageSummaryDto>> getMypageSummary(Principal principal) {
		String currentLoginId = principal.getName();
		// 서비스가 복호화 + 식별번호 마스킹까지 처리해서 반환한다.
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
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

	// 내 정보 조회 (식별번호 마스킹은 서비스가 처리)
	@GetMapping("/myinfo")
	public ResponseEntity<ApiResponse<BankMemberDto>> getMyInfo(Principal principal) {
		String currentLoginId = principal.getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		return ResponseEntity.ok(ApiResponse.ok(memberInfo));
	}

	// 내 계좌 목록 조회
	@GetMapping("/myaccounts")
	public ResponseEntity<ApiResponse<List<AccountDto>>> getMyAccounts(Principal principal) {
		String currentLoginId = principal.getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		long currentMemberNo = memberInfo.getMember_no();

		List<AccountDto> accountList = accountService.getAccounts(currentMemberNo);
		return ResponseEntity.ok(ApiResponse.ok(accountList));
	}

	// 계좌 상세 + 거래내역 조회
	@GetMapping("/accounts/{accountNo}/history")
	public ResponseEntity<ApiResponse<AccountHistoryDto>> getAccountHistory(@PathVariable Long accountNo) {
		AccountDto account = accountService.getAccountDetail(accountNo);
		List<AccountTransactionDto> transactionList = accountTransactionService.getTransactions(accountNo);

		AccountHistoryDto historyData = AccountHistoryDto.builder()
				.account(account)
				.transactionList(transactionList)
				.build();

		return ResponseEntity.ok(ApiResponse.ok(historyData));
	}

	// 가입 상품 내역 조회
	@GetMapping("/myproducts")
	public ResponseEntity<ApiResponse<List<MemberProductDto>>> getMyProducts(Principal principal) {
		String currentLoginId = principal.getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		long currentMemberNo = memberInfo.getMember_no();

		List<MemberProductDto> productList = productSalesService.getSubscribedProducts(currentMemberNo);
		return ResponseEntity.ok(ApiResponse.ok(productList));
	}

	// ===================== 변경(POST) =====================

	// 내 정보 수정
	@PostMapping("/myinfo/update")
	public ResponseEntity<ApiResponse<Void>> updateMyInfo(
			Principal principal,
			@RequestParam(value = "phone_number", defaultValue = "") String phoneNumber,
			@RequestParam(value = "email", defaultValue = "") String email,
			@RequestParam(value = "address_main", defaultValue = "") String addressMain,
			@RequestParam(value = "address_detail", defaultValue = "") String addressDetail) {

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