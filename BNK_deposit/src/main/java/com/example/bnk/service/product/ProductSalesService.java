package com.example.bnk.service.product;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IAccountDao;
import com.example.bnk.dao.member.ISecurityCardDao;
import com.example.bnk.dao.product.IProductSalesDao;
import com.example.bnk.dto.member.AccountCreateDto;
import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.MemberProductDto;
import com.example.bnk.dto.member.SecurityCardDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductJoinDraftDto;
import com.example.bnk.dto.product.ProductJoinStatusDto;
import com.example.bnk.utils.AesCryptoUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductSalesService {

	private static final int SECURITY_CARD_NUMBER_COUNT = 20;
	private static final int SECURITY_CARD_PAIR_SIZE = 2;
	private static final int SECURITY_CARD_PAIR_COUNT = SECURITY_CARD_NUMBER_COUNT / SECURITY_CARD_PAIR_SIZE;

	private final IProductSalesDao productSalesDao;
	private final IAccountDao accountDao;
	private final ISecurityCardDao securityCardDao;
	private final ProductViewService productViewService;
	private final AesCryptoUtil aesCryptoUtil;
	private final SecureRandom secureRandom = new SecureRandom();

	public int getSubscribedProductCount(long memberNo) {
		return productSalesDao.countProductSalesByMemberNo(memberNo);
	}

	public List<MemberProductDto> getSubscribedProducts(String username) {
		return productSalesDao.findSubscribedProductsByMemberNo(username);
	}

	public int upTermscommunityRegist(@Param("member_no") long member_no) {
		int result = productSalesDao.upTermsCommunityRegist(member_no);

		if (result == 1) {
			System.out.println("?곕?湲덈━ ?곸듅 ?깃났");
			return 1;
		}
		System.out.println("?곕?湲덈━ ?곸듅 ?ㅽ뙣");
		return 0;
	}

	public ProductJoinStatusDto startJoin(long memberNo, long productNo) {
		ProductDetailViewDto product = requireJoinableProduct(productNo);
		boolean accountRequired = accountDao.findActiveAccountNoByMemberNo(memberNo) == null;

		if (accountRequired) {
			return buildEmptyStatus(product, "ACCOUNT_REQUIRED", true);
		}

		ProductJoinDraftDto latest = productSalesDao.findLatestByMemberAndProduct(memberNo, productNo);

		if (latest == null) {
			return buildEmptyStatus(product, "TERMS", false);
		}

		return toStatus(latest, product, false);
	}

	public ProductJoinStatusDto getDraftStatus(long memberNo, long productNo) {
		return startJoin(memberNo, productNo);
	}

	public ProductJoinStatusDto saveTerms(
			long memberNo,
			long productNo,
			boolean requiredTermsAgreed,
			boolean optionalTermsAgreed
	) {
		if (!requiredTermsAgreed) {
			throw new IllegalArgumentException("필수 약관 동의가 필요합니다.");
		}

		ProductDetailViewDto product = requireJoinableProduct(productNo);
		ProductJoinDraftDto latest = productSalesDao.findLatestByMemberAndProduct(memberNo, productNo);

		if (latest != null && "COMPLETE".equals(latest.getSubscription_status())) {
			throw new IllegalStateException("이미 가입한 상품입니다.");
		}

		if (latest == null || !"DRAFT".equals(latest.getSubscription_status())) {
			productSalesDao.insertDraft(memberNo, productNo);
		} else if (latest.getRequired_terms_agreed() == null || latest.getRequired_terms_agreed() != 1) {
			productSalesDao.updateDraftTermsAgreement(
					memberNo,
					latest.getSubscription_no(),
					1,
					optionalTermsAgreed ? 1 : 0
			);
		}

		ProductJoinDraftDto draft = productSalesDao.findDraftByMemberAndProduct(memberNo, productNo);

		return toStatus(draft, product, accountDao.findActiveAccountNoByMemberNo(memberNo) == null);
	}

	public ProductJoinStatusDto updateContract(
			long memberNo,
			long subscriptionNo,
			long linkedAccountNo,
			long subscriptionAmount,
			long subscriptionMonths
	) {
		ProductJoinDraftDto draft = requireDraft(memberNo, subscriptionNo);
		ProductDetailViewDto product = requireJoinableProduct(draft.getProduct_no());

		if (draft.getRequired_terms_agreed() == null || draft.getRequired_terms_agreed() != 1) {
			throw new IllegalStateException("필수 약관 동의가 필요합니다.");
		}

		AccountDto linkedAccount = requireActiveMemberAccount(memberNo, linkedAccountNo);
		validateAmountAndMonths(product, subscriptionAmount, subscriptionMonths);

		if (linkedAccount.getBalance() < subscriptionAmount) {
			throw new IllegalStateException("출금계좌 잔액이 부족합니다.");
		}

		int updated = productSalesDao.updateDraftContract(
				memberNo,
				subscriptionNo,
				linkedAccountNo,
				subscriptionAmount,
				subscriptionMonths,
				subscriptionAmount,
				product.getMin_interest_rate()
		);

		if (updated != 1) {
			throw new IllegalStateException("계약내용 저장에 실패했습니다.");
		}

		return toStatus(productSalesDao.findBySubscriptionNo(memberNo, subscriptionNo), product, false);
	}

	public ProductJoinStatusDto completeJoin(
			long memberNo,
			long subscriptionNo,
			String accountPassword,
			Integer frontIndex,
			Integer backIndex,
			String frontAnswer,
			String backAnswer
	) {
		ProductJoinDraftDto draft = productSalesDao.findBySubscriptionNo(memberNo, subscriptionNo);

		if (draft == null) {
			throw new IllegalArgumentException("진행 중인 상품 가입 내역을 찾을 수 없습니다.");
		}

		ProductDetailViewDto product = requireJoinableProduct(draft.getProduct_no());

		if ("COMPLETE".equals(draft.getSubscription_status())) {
			return toStatus(draft, product, false);
		}

		if (!"DRAFT".equals(draft.getSubscription_status())) {
			throw new IllegalArgumentException("진행 중인 상품 가입 내역을 찾을 수 없습니다.");
		}

		validateCompleteReady(draft);
		validateProductAccountPassword(accountPassword);

		AccountDto linkedAccount = requireActiveMemberAccount(memberNo, draft.getLinked_account_id());
		validateAmountAndMonths(product, draft.getSubscription_amount(), draft.getSubscription_months());

		if (linkedAccount.getBalance() < draft.getSubscription_amount()) {
			throw new IllegalStateException("출금계좌 잔액이 부족합니다.");
		}

		verifySecurityCard(memberNo, frontIndex, backIndex, frontAnswer, backAnswer);

		Long productAccountNo = draft.getAccount_no();

		if (productAccountNo == null) {
			int debited = accountDao.debitAccount(linkedAccount.getAccount_no(), memberNo, draft.getSubscription_amount());

			if (debited != 1) {
				throw new IllegalStateException("출금계좌 잔액이 부족하거나 출금에 실패했습니다.");
			}

			AccountCreateDto productAccount = new AccountCreateDto();
			productAccount.setMemberNo(memberNo);
			productAccount.setAccountAlias(product.getProduct_name());
			productAccount.setAccountPurpose(resolveProductAccountPurpose(product));
			productAccount.setAccountPassword(aesCryptoUtil.encryptGcmToString(accountPassword));
			productAccount.setBalance(draft.getSubscription_amount());

			accountDao.insertProductAccount(productAccount);

			if (productAccount.getAccountNo() == null) {
				throw new IllegalStateException("상품 계좌 생성에 실패했습니다.");
			}

			productAccountNo = productAccount.getAccountNo();
		}

		int completed = productSalesDao.completeDraft(
				memberNo,
				subscriptionNo,
				productAccountNo,
				draft.getApplied_interest_rate(),
				draft.getMaturity_date()
		);

		if (completed != 1) {
			throw new IllegalStateException("상품 가입 완료 처리에 실패했습니다.");
		}

		return toStatus(productSalesDao.findBySubscriptionNo(memberNo, subscriptionNo), product, false);
	}

	public java.util.Map<String, Object> createSecurityCardChallenge(long memberNo) {
		requireSecurityCard(memberNo);
		int frontIndex = secureRandom.nextInt(SECURITY_CARD_PAIR_COUNT) + 1;
		int backIndex = secureRandom.nextInt(SECURITY_CARD_PAIR_COUNT) + 1;

		while (backIndex == frontIndex) {
			backIndex = secureRandom.nextInt(SECURITY_CARD_PAIR_COUNT) + 1;
		}

		return java.util.Map.of(
				"frontIndex", frontIndex,
				"backIndex", backIndex,
				"message", frontIndex + "번 앞자리와 " + backIndex + "번 뒷자리를 입력해주세요."
		);
	}

	private ProductJoinStatusDto buildEmptyStatus(ProductDetailViewDto product, String currentStep, boolean accountRequired) {
		return ProductJoinStatusDto.builder()
				.productNo(product.getProduct_no())
				.productName(product.getProduct_name())
				.productType(product.getProduct_type())
				.minJoinAmount(product.getMin_join_amount())
				.maxJoinAmount(product.getMax_join_amount())
				.depositUnit(product.getDeposit_unit())
				.minTermMonths(product.getMin_term_months())
				.maxTermMonths(product.getMax_term_months())
				.subscriptionStatus("NONE")
				.currentStep(currentStep)
				.accountRequired(accountRequired)
				.requiredTermsAgreed(0)
				.optionalTermsAgreed(0)
				.message(resolveMessage(currentStep))
				.build();
	}

	private ProductJoinDraftDto requireDraft(long memberNo, long subscriptionNo) {
		ProductJoinDraftDto draft = productSalesDao.findBySubscriptionNo(memberNo, subscriptionNo);

		if (draft == null || !"DRAFT".equals(draft.getSubscription_status())) {
			throw new IllegalArgumentException("진행 중인 상품 가입 내역을 찾을 수 없습니다.");
		}

		return draft;
	}

	private ProductDetailViewDto requireJoinableProduct(long productNo) {
		ProductDetailViewDto product = productViewService.getProductDetail(productNo);

		if (product == null) {
			throw new IllegalArgumentException("상품 정보를 찾을 수 없습니다.");
		}

		if (!"SALE".equals(product.getProduct_status())) {
			throw new IllegalStateException("판매 중인 상품만 가입할 수 있습니다.");
		}

		if (!"Y".equalsIgnoreCase(product.getMobile_join_yn())) {
			throw new IllegalStateException("모바일 가입이 가능한 상품만 앱에서 가입할 수 있습니다.");
		}

		return product;
	}

	private AccountDto requireActiveMemberAccount(long memberNo, Long accountNo) {
		if (accountNo == null) {
			throw new IllegalStateException("출금계좌 선택이 필요합니다.");
		}

		AccountDto account = accountDao.findAccountByAccountNoAndMemberNo(accountNo, memberNo);

		if (account == null) {
			throw new IllegalStateException("사용 가능한 출금계좌를 찾을 수 없습니다.");
		}

		return account;
	}

	private void validateCompleteReady(ProductJoinDraftDto draft) {
		if (draft.getRequired_terms_agreed() == null || draft.getRequired_terms_agreed() != 1) {
			throw new IllegalStateException("필수 약관 동의가 필요합니다.");
		}

		if (draft.getLinked_account_id() == null
				|| draft.getSubscription_months() == null
				|| draft.getSubscription_amount() == null
				|| draft.getAuto_transfer_amount() == null
				|| draft.getApplied_interest_rate() == null
				|| draft.getMaturity_date() == null) {
			throw new IllegalStateException("계약내용 확인이 먼저 필요합니다.");
		}
	}

	private void validateProductAccountPassword(String accountPassword) {
		if (accountPassword == null || !accountPassword.matches("\\d{4}")) {
			throw new IllegalArgumentException("계좌 비밀번호는 숫자 4자리로 입력해주세요.");
		}
	}

	private void verifySecurityCard(
			long memberNo,
			Integer frontIndex,
			Integer backIndex,
			String frontAnswer,
			String backAnswer
	) {
		List<List<String>> pairs = securityCardPairs(memberNo);
		int front = normalizeSecurityCardIndex(frontIndex, "front");
		int back = normalizeSecurityCardIndex(backIndex, "back");

		String expectedFront = pairs.get(front).get(0);
		String expectedBack = pairs.get(back).get(1);

		if (!expectedFront.equals(cleanAnswer(frontAnswer)) || !expectedBack.equals(cleanAnswer(backAnswer))) {
			throw new IllegalArgumentException("보안카드 번호가 일치하지 않습니다.");
		}
	}

	private int normalizeSecurityCardIndex(Integer index, String label) {
		if (index == null || index < 1 || index > SECURITY_CARD_PAIR_COUNT) {
			throw new IllegalArgumentException("보안카드 " + label + " 요청 번호가 올바르지 않습니다.");
		}

		return index - 1;
	}

	private List<List<String>> securityCardPairs(long memberNo) {
		SecurityCardDto card = requireSecurityCard(memberNo);
		String plain = card.getSec_num().split("\\.", -1).length == 3
				? aesCryptoUtil.decryptGcmToString(card.getSec_num())
				: aesCryptoUtil.decrypt(card.getSec_num());
		List<String> numbers = List.of(plain.trim().split("\\s+"));

		if (numbers.size() != SECURITY_CARD_NUMBER_COUNT) {
			throw new IllegalStateException("보안카드 형식이 올바르지 않습니다.");
		}

		List<List<String>> pairs = new ArrayList<>();

		for (int i = 0; i < numbers.size(); i += SECURITY_CARD_PAIR_SIZE) {
			pairs.add(List.of(numbers.get(i), numbers.get(i + 1)));
		}

		return pairs;
	}

	private SecurityCardDto requireSecurityCard(long memberNo) {
		SecurityCardDto card = securityCardDao.findActiveByMemberNo(memberNo);

		if (card == null || card.getSec_num() == null || card.getSec_num().isBlank()) {
			throw new IllegalStateException("사용 가능한 보안카드가 없습니다.");
		}

		return card;
	}

	private String cleanAnswer(String value) {
		return value == null ? "" : value.replaceAll("\\D", "");
	}

	private void validateAmountAndMonths(ProductDetailViewDto product, long amount, long months) {
		if (amount < product.getMin_join_amount()) {
			throw new IllegalArgumentException("최소 가입금액 이상을 입력해주세요.");
		}

		if (product.getMax_join_amount() > 0 && amount > product.getMax_join_amount()) {
			throw new IllegalArgumentException("최대 가입금액 이하로 입력해주세요.");
		}

		if (product.getDeposit_unit() > 0 && amount % product.getDeposit_unit() != 0) {
			throw new IllegalArgumentException("가입금액 단위에 맞게 입력해주세요.");
		}

		if (months < product.getMin_term_months() || months > product.getMax_term_months()) {
			throw new IllegalArgumentException("가입기간 범위에 맞게 선택해주세요.");
		}
	}

	private ProductJoinStatusDto toStatus(
			ProductJoinDraftDto draft,
			ProductDetailViewDto product,
			boolean accountRequired
	) {
		String currentStep = resolveStep(draft, accountRequired);

		return ProductJoinStatusDto.builder()
				.subscriptionNo(draft.getSubscription_no())
				.productNo(product.getProduct_no())
				.productName(product.getProduct_name())
				.productType(product.getProduct_type())
				.minJoinAmount(product.getMin_join_amount())
				.maxJoinAmount(product.getMax_join_amount())
				.depositUnit(product.getDeposit_unit())
				.minTermMonths(product.getMin_term_months())
				.maxTermMonths(product.getMax_term_months())
				.subscriptionStatus(draft.getSubscription_status())
				.currentStep(currentStep)
				.accountRequired(accountRequired)
				.accountNo(draft.getAccount_no())
				.linkedAccountId(draft.getLinked_account_id())
				.subscriptionAmount(draft.getSubscription_amount())
				.subscriptionMonths(draft.getSubscription_months())
				.appliedInterestRate(draft.getApplied_interest_rate())
				.requiredTermsAgreed(draft.getRequired_terms_agreed())
				.optionalTermsAgreed(draft.getOptional_terms_agreed())
				.maturityDate(draft.getMaturity_date())
				.message(resolveMessage(currentStep))
				.build();
	}

	private String resolveStep(ProductJoinDraftDto draft, boolean accountRequired) {
		if ("COMPLETE".equals(draft.getSubscription_status())) {
			return "COMPLETE";
		}

		if ("EXPIRED".equals(draft.getSubscription_status())) {
			return "EXPIRED";
		}

		if (accountRequired) {
			return "ACCOUNT_REQUIRED";
		}

		if (draft.getRequired_terms_agreed() == null || draft.getRequired_terms_agreed() != 1) {
			return "TERMS";
		}

		if (draft.getLinked_account_id() == null
				|| draft.getSubscription_amount() == null
				|| draft.getSubscription_months() == null
				|| draft.getAuto_transfer_amount() == null) {
			return "CONTRACT_INPUT";
		}

		return "CONTRACT_CONFIRM";
	}

	private String resolveMessage(String currentStep) {
		return switch (currentStep) {
			case "ACCOUNT_REQUIRED" -> "상품 가입을 위해서는 입출금 계좌 개설이 필요합니다.";
			case "TERMS" -> "약관을 확인하고 동의해주세요.";
			case "CONTRACT_INPUT" -> "가입조건을 입력해주세요.";
			case "CONTRACT_CONFIRM" -> "계약내용을 확인해주세요.";
			case "COMPLETE" -> "이미 가입한 상품입니다.";
			case "EXPIRED" -> "만기 또는 해지된 가입 이력이 있습니다.";
			default -> null;
		};
	}

	private String resolveProductAccountPurpose(ProductDetailViewDto product) {
		if ("DEPOSIT".equals(product.getProduct_type())) {
			return "DEPOSIT";
		}

		if ("SAVINGS".equals(product.getProduct_type())) {
			return "SAVINGS";
		}

		return "ETC";
	}

}
