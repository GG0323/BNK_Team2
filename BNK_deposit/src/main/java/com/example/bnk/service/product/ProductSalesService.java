package com.example.bnk.service.product;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IAccountDao;
import com.example.bnk.dao.product.IProductSalesDao;
import com.example.bnk.dto.member.AccountCreateDto;
import com.example.bnk.dto.member.MemberProductDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductJoinDraftDto;
import com.example.bnk.dto.product.ProductJoinStatusDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductSalesService {

	private static final Set<String> ACCOUNT_PURPOSE_CODES = Set.of(
			"SALARY",
			"PART_TIME_SALARY",
			"PENSION",
			"BUSINESS",
			"GROUP",
			"UTILITY_PAYMENT",
			"LIVING_EXPENSE",
			"ETC"
	);

    private final IProductSalesDao productSalesDao;
    private final IAccountDao accountDao;
    private final ProductViewService productViewService;

    // 마이페이지에서 사용자가 가입한 상품의 수 조회
    public int getSubscribedProductCount(long memberNo) {
        return productSalesDao.countProductSalesByMemberNo(memberNo);
    }
    
    // 사용자가 가입한 상품의 수 출력
    public List<MemberProductDto> getSubscribedProducts(String username) {
        return productSalesDao.findSubscribedProductsByMemberNo(username);
    }
    
	
    // 커뮤니티 가입 시 우대금리 상승 서비스
	public int upTermscommunityRegist(@Param("member_no") long member_no) {
		int result = productSalesDao.upTermsCommunityRegist(member_no);
		
		if(result == 1) {
			System.out.println("우대금리 상승 성공");
			return 1;
		}
		System.out.println("우대금리 상승 실패");
		return 0;
	}

    public ProductJoinStatusDto startJoin(long memberNo, long productNo) {
        ProductDetailViewDto product = requireJoinableProduct(productNo);
        Long linkedAccountId = accountDao.findActiveAccountNoByMemberNo(memberNo);

        if (linkedAccountId == null) {
            return buildAccountRequiredStatus(product);
        }

        ProductJoinDraftDto draft = productSalesDao.findDraftByMemberAndProduct(memberNo, productNo);

        if (draft == null) {
            productSalesDao.insertDraft(memberNo, productNo, linkedAccountId);
            draft = productSalesDao.findDraftByMemberAndProduct(memberNo, productNo);
        }

        return toStatus(draft, product, linkedAccountId == null);
    }

    private ProductJoinStatusDto buildAccountRequiredStatus(ProductDetailViewDto product) {
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
                .currentStep("ACCOUNT_REQUIRED")
                .accountRequired(true)
                .message(resolveMessage("ACCOUNT_REQUIRED"))
                .build();
    }

    public ProductJoinStatusDto getDraftStatus(long memberNo, long productNo) {
        ProductDetailViewDto product = requireJoinableProduct(productNo);
        ProductJoinDraftDto draft = productSalesDao.findDraftByMemberAndProduct(memberNo, productNo);

        if (draft == null) {
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
                    .currentStep(accountDao.findActiveAccountNoByMemberNo(memberNo) == null ? "ACCOUNT_REQUIRED" : "NOT_STARTED")
                    .accountRequired(accountDao.findActiveAccountNoByMemberNo(memberNo) == null)
                    .message("진행 중인 상품 가입 내역이 없습니다.")
                    .build();
        }

        return toStatus(draft, product, accountDao.findActiveAccountNoByMemberNo(memberNo) == null);
    }

    public ProductJoinStatusDto saveTerms(
            long memberNo,
            long subscriptionNo,
            long subscriptionAmount,
            long subscriptionMonths,
            boolean requiredTermsAgreed,
            boolean optionalTermsAgreed
    ) {
        ProductJoinDraftDto draft = requireDraft(memberNo, subscriptionNo);
        ProductDetailViewDto product = requireJoinableProduct(draft.getProduct_no());

        if (!requiredTermsAgreed) {
            throw new IllegalArgumentException("필수 약관 동의가 필요합니다.");
        }

        validateAmountAndMonths(product, subscriptionAmount, subscriptionMonths);

        productSalesDao.updateDraftTerms(
                memberNo,
                subscriptionNo,
                subscriptionAmount,
                subscriptionMonths,
                1,
                optionalTermsAgreed ? 1 : 0
        );

        return toStatus(
                productSalesDao.findBySubscriptionNo(memberNo, subscriptionNo),
                product,
                accountDao.findActiveAccountNoByMemberNo(memberNo) == null
        );
    }

    public ProductJoinStatusDto completeJoin(long memberNo, long subscriptionNo, String accountPurpose) {
        ProductJoinDraftDto draft = requireDraft(memberNo, subscriptionNo);
        ProductDetailViewDto product = requireJoinableProduct(draft.getProduct_no());
        Long linkedAccountId = accountDao.findActiveAccountNoByMemberNo(memberNo);

        if (linkedAccountId == null) {
            throw new IllegalStateException("상품 가입 전 입출금 계좌 개설이 필요합니다.");
        }

        if (draft.getRequired_terms_agreed() == null || draft.getRequired_terms_agreed() != 1) {
            throw new IllegalStateException("필수 약관 동의가 필요합니다.");
        }

        if (draft.getSubscription_amount() == null || draft.getSubscription_months() == null) {
            throw new IllegalStateException("가입 금액과 기간 입력이 필요합니다.");
        }

        validateAccountPurpose(accountPurpose);

        AccountCreateDto account = new AccountCreateDto();
        account.setMemberNo(memberNo);
        account.setAccountAlias(product.getProduct_name());
        account.setAccountPurpose(blankToNull(accountPurpose));
        accountDao.insertProductAccount(account);

        if (account.getAccountNo() == null) {
            throw new IllegalStateException("상품 계좌 생성에 실패했습니다.");
        }

        productSalesDao.completeDraft(
                memberNo,
                subscriptionNo,
                account.getAccountNo(),
                product.getMax_interest_rate(),
                LocalDate.now().plusMonths(draft.getSubscription_months())
        );

        return toStatus(
                productSalesDao.findBySubscriptionNo(memberNo, subscriptionNo),
                product,
                false
        );
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

    private void validateAmountAndMonths(ProductDetailViewDto product, long amount, long months) {
        if (amount < product.getMin_join_amount()) {
            throw new IllegalArgumentException("최소 가입금액 이상을 입력해 주세요.");
        }

        if (product.getMax_join_amount() > 0 && amount > product.getMax_join_amount()) {
            throw new IllegalArgumentException("최대 가입금액 이하로 입력해 주세요.");
        }

        if (product.getDeposit_unit() > 0 && amount % product.getDeposit_unit() != 0) {
            throw new IllegalArgumentException("가입금액 단위에 맞게 입력해 주세요.");
        }

        if (months < product.getMin_term_months() || months > product.getMax_term_months()) {
            throw new IllegalArgumentException("가입 기간 범위에 맞게 입력해 주세요.");
        }
    }

    private void validateAccountPurpose(String accountPurpose) {
        String normalized = blankToNull(accountPurpose);

        if (normalized != null && !ACCOUNT_PURPOSE_CODES.contains(normalized)) {
            throw new IllegalArgumentException("올바른 계좌 사용 목적을 선택해 주세요.");
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

        if (accountRequired) {
            return "ACCOUNT_REQUIRED";
        }

        if (draft.getRequired_terms_agreed() == null || draft.getRequired_terms_agreed() != 1) {
            return "TERMS";
        }

        if (draft.getSubscription_amount() == null || draft.getSubscription_months() == null) {
            return "TERMS";
        }

        return "READY_TO_COMPLETE";
    }

    private String resolveMessage(String currentStep) {
        return switch (currentStep) {
            case "ACCOUNT_REQUIRED" -> "상품 가입 전 입출금 계좌 개설이 필요합니다.";
            case "TERMS" -> "가입 조건과 약관 동의를 입력해 주세요.";
            case "READY_TO_COMPLETE" -> "상품 가입을 완료할 수 있습니다.";
            case "COMPLETE" -> "상품 가입이 완료되었습니다.";
            default -> null;
        };
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }
}
