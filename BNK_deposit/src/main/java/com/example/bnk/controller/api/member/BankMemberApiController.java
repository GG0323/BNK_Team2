package com.example.bnk.controller.api.member;

import java.security.Principal;
import java.time.LocalDate;
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
 * 회원 마이페이지 관련 "데이터 처리"를 전담하는 REST API 컨트롤러. (현재 MOCK 모드)
 *
 * ⚠ 현재 상태:
 *   로그인/회원가입 담당자가 TB_BANK_MEMBER 작업 중이라 실제 DB 조회를 할 수 없어,
 *   각 GET 메서드를 손으로 만든 MOCK 데이터로 응답하도록 임시 처리했다.
 *   MOCK 데이터는 모두 실제 DTO 필드명과 일치시켜, 진짜 데이터 전환 시 화면이 그대로 동작한다.
 *
 *   ▶ 복구 방법: 각 메서드의 "===== MOCK START/END =====" 블록을 삭제하고,
 *     바로 아래 주석 처리된 "REAL" 코드를 살리면 된다.
 *
 *   POST(수정/비번변경)는 DB 쓰기라서 MOCK 처리하지 않고 실제 로직을 유지한다.
 *   (담당자 작업 충돌 방지를 위해 실제로 호출하기 전 주의)
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

	// ============================================================
	// MOCK 데이터 생성 헬퍼 (담당자 작업 완료 후 이 영역 전체 삭제 가능)
	// ============================================================

	private BankMemberDto mockMember() {
		BankMemberDto m = new BankMemberDto();
		m.setMember_no(1L);
		m.setLogin_id("test01");
		m.setMember_name("홍길동");
		m.setMember_type("PERSONAL");
		// 서비스가 평소 만들어주는 마스킹 형태를 그대로 흉내
		m.setMember_identifier("990101-1******");
		m.setGender("M");
		m.setBirth_date(LocalDate.of(1999, 1, 1));
		m.setPhone_number("010-1234-5678");
		m.setEmail("hong@naver.com");
		m.setAdress("부산광역시 부산진구 중앙대로 999");
		m.setCredit_score(720);
		m.setMember_status("REGULAR");
		m.setCreated_at(LocalDate.of(2026, 1, 15));
		m.setLast_login_at(LocalDate.of(2026, 5, 22));
		return m;
	}

	private List<AccountDto> mockAccounts() {
		AccountDto a1 = new AccountDto();
		a1.setAccount_no(101L);
		a1.setMember_no(1L);
		a1.setAccount_number(123456789L);
		a1.setAccount_alias("생활비 통장");
		a1.setBalance(2_300_000L);
		a1.setAccount_status("ACTIVE");
		a1.setOpened_at(LocalDate.of(2026, 2, 1));

		AccountDto a2 = new AccountDto();
		a2.setAccount_no(102L);
		a2.setMember_no(1L);
		a2.setAccount_number(987654321L);
		a2.setAccount_alias("비상금");
		a2.setBalance(4_260_000L);
		a2.setAccount_status("ACTIVE");
		a2.setOpened_at(LocalDate.of(2026, 3, 10));

		return List.of(a1, a2);
	}

	private List<AccountTransactionDto> mockTransactions() {
		AccountTransactionDto t1 = new AccountTransactionDto();
		t1.setTransaction_no(1001L);
		t1.setAmount(3_250_000L);
		t1.setTransaction_type("DEPOSIT");
		t1.setBalance_after(4_250_000L);
		t1.setMemo("5월 급여");
		t1.setCounterparty_name("급여입금");
		t1.setTransaction_at(LocalDate.of(2026, 5, 19));

		AccountTransactionDto t2 = new AccountTransactionDto();
		t2.setTransaction_no(1002L);
		t2.setAmount(50_000L);
		t2.setTransaction_type("WITHDRAW");
		t2.setBalance_after(4_200_000L);
		t2.setMemo("ATM 출금");
		t2.setCounterparty_name("ATM");
		t2.setTransaction_at(LocalDate.of(2026, 5, 20));

		return List.of(t1, t2);
	}

	private List<MemberProductDto> mockProducts() {
		MemberProductDto p1 = new MemberProductDto();
		p1.setSubscription_no(5001L);
		p1.setMember_no(1L);
		p1.setSubscription_amount(1_000_000L);
		p1.setApplied_interest_rate(5.00);
		p1.setMaturity_date(LocalDate.of(2027, 6, 1));
		p1.setSubscription_status("COMPLETE");
		p1.setProduct_name("모바일 전용 적금");
		p1.setProduct_type("SAVINGS");

		MemberProductDto p2 = new MemberProductDto();
		p2.setSubscription_no(5002L);
		p2.setMember_no(1L);
		p2.setSubscription_amount(5_000_000L);
		p2.setApplied_interest_rate(3.20);
		p2.setMaturity_date(LocalDate.of(2027, 1, 10));
		p2.setSubscription_status("COMPLETE");
		p2.setProduct_name("정기 예금");
		p2.setProduct_type("DEPOSIT");

		return List.of(p1, p2);
	}

	private List<MemberTrackingLogDto> mockLogs() {
		MemberTrackingLogDto l1 = new MemberTrackingLogDto();
		l1.setMember_tracking_log_no(1L);
		l1.setRequested_page("마이페이지 접속");
		l1.setRequest_ip("127.0.0.1");
		l1.setAccessed_at(LocalDate.of(2026, 5, 22));

		MemberTrackingLogDto l2 = new MemberTrackingLogDto();
		l2.setMember_tracking_log_no(2L);
		l2.setRequested_page("계좌 조회");
		l2.setRequest_ip("127.0.0.1");
		l2.setAccessed_at(LocalDate.of(2026, 5, 21));

		return List.of(l1, l2);
	}

	// ===================== 조회(GET) =====================

	// 마이페이지 요약 데이터
	@GetMapping("/mypage")
	public ResponseEntity<ApiResponse<MypageSummaryDto>> getMypageSummary(Principal principal) {
		// ===== MOCK START =====
		List<AccountDto> mockAccounts = mockAccounts();
		MypageSummaryDto summary = MypageSummaryDto.builder()
				.member(mockMember())
				.accountCount(mockAccounts.size())
				.totalBalance(mockAccounts.stream().mapToLong(AccountDto::getBalance).sum())
				.productCount(mockProducts().size())
				.logCount(mockLogs().size())
				.recentLogs(mockLogs())
				.build();
		return ResponseEntity.ok(ApiResponse.ok(summary));
		// ===== MOCK END =====

		/* ===== REAL (담당자 작업 완료 후 복구) =====
		String currentLoginId = principal.getName();
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
		*/
	}

	// 내 정보 조회
	@GetMapping("/myinfo")
	public ResponseEntity<ApiResponse<BankMemberDto>> getMyInfo(Principal principal) {
		// ===== MOCK START =====
		return ResponseEntity.ok(ApiResponse.ok(mockMember()));
		// ===== MOCK END =====

		/* ===== REAL =====
		String currentLoginId = principal.getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		return ResponseEntity.ok(ApiResponse.ok(memberInfo));
		*/
	}

	// 내 계좌 목록 조회
	@GetMapping("/myaccounts")
	public ResponseEntity<ApiResponse<List<AccountDto>>> getMyAccounts(Principal principal) {
		// ===== MOCK START =====
		return ResponseEntity.ok(ApiResponse.ok(mockAccounts()));
		// ===== MOCK END =====

		/* ===== REAL =====
		String currentLoginId = principal.getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		long currentMemberNo = memberInfo.getMember_no();
		List<AccountDto> accountList = accountService.getAccounts(currentMemberNo);
		return ResponseEntity.ok(ApiResponse.ok(accountList));
		*/
	}

	// 계좌 상세 + 거래내역 조회
	@GetMapping("/accounts/{accountNo}/history")
	public ResponseEntity<ApiResponse<AccountHistoryDto>> getAccountHistory(@PathVariable Long accountNo) {
		// ===== MOCK START =====
		// accountNo 에 해당하는 계좌를 mock 목록에서 찾고, 없으면 첫 번째로 대체
		AccountDto account = mockAccounts().stream()
				.filter(a -> a.getAccount_no() == accountNo)
				.findFirst()
				.orElse(mockAccounts().get(0));
		AccountHistoryDto historyData = AccountHistoryDto.builder()
				.account(account)
				.transactionList(mockTransactions())
				.build();
		return ResponseEntity.ok(ApiResponse.ok(historyData));
		// ===== MOCK END =====

		/* ===== REAL =====
		AccountDto account = accountService.getAccountDetail(accountNo);
		List<AccountTransactionDto> transactionList = accountTransactionService.getTransactions(accountNo);
		AccountHistoryDto historyData = AccountHistoryDto.builder()
				.account(account)
				.transactionList(transactionList)
				.build();
		return ResponseEntity.ok(ApiResponse.ok(historyData));
		*/
	}

	// 가입 상품 내역 조회
	@GetMapping("/myproducts")
	public ResponseEntity<ApiResponse<List<MemberProductDto>>> getMyProducts(Principal principal) {
		// ===== MOCK START =====
		return ResponseEntity.ok(ApiResponse.ok(mockProducts()));
		// ===== MOCK END =====

		/* ===== REAL =====
		String currentLoginId = principal.getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		long currentMemberNo = memberInfo.getMember_no();
		List<MemberProductDto> productList = productSalesService.getSubscribedProducts(currentMemberNo);
		return ResponseEntity.ok(ApiResponse.ok(productList));
		*/
	}

	// ===================== 변경(POST) =====================
	// POST 는 DB 쓰기라 MOCK 처리하지 않는다.
	// 담당자가 TB_BANK_MEMBER 작업 중이므로, 실제 수정 테스트는 작업 완료 후 진행할 것.

	// 내 정보 수정
	@PostMapping("/myinfo/update")
	public ResponseEntity<ApiResponse<Void>> updateMyInfo(
			Principal principal,
			@RequestParam(value = "phone_number", defaultValue = "") String phoneNumber,
			@RequestParam(value = "email", defaultValue = "") String email,
			@RequestParam(value = "address_main", defaultValue = "") String addressMain,
			@RequestParam(value = "address_detail", defaultValue = "") String addressDetail) {

		// ===== MOCK 가드 (비로그인 테스트용) =====
		// 로그인 전이라 principal 이 null 이면 DB 를 건드리지 않고 성공 응답만 흉내낸다.
		// 담당자 작업 완료 후에는 이 블록을 삭제할 것.
		if (principal == null) {
			return ResponseEntity.ok(ApiResponse.success("[MOCK] 개인정보가 성공적으로 수정되었습니다."));
		}
		// ===== MOCK 가드 END =====

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

		// ===== MOCK 가드 (비로그인 테스트용, 작업 완료 후 삭제) =====
		if (principal == null) {
			// 화면 흐름 확인용: 현재 비번이 "1234" 면 성공, 아니면 실패로 흉내
			if ("1234".equals(currentPassword)) {
				return ResponseEntity.ok(ApiResponse.success("[MOCK] 비밀번호가 성공적으로 변경되었습니다."));
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.fail("[MOCK] 현재 비밀번호가 일치하지 않습니다. (테스트: 1234 입력)"));
		}
		// ===== MOCK 가드 END =====

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