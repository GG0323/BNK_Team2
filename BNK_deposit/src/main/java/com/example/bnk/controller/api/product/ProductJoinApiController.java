package com.example.bnk.controller.api.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.auth.MemberDetails;
import com.example.bnk.dao.member.IAccountDao;
import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductJoinEntryStatusDto;
import com.example.bnk.dto.product.ProductJoinRequests.CompleteRequest;
import com.example.bnk.dto.product.ProductJoinRequests.StartRequest;
import com.example.bnk.dto.product.ProductJoinRequests.TermsRequest;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.product.ProductSalesService;
import com.example.bnk.service.product.ProductViewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products/join")
@RequiredArgsConstructor
public class ProductJoinApiController {

	private final ProductSalesService productSalesService;
	private final ProductViewService productViewService;
	private final BankMemberService bankMemberService;
	private final IAccountDao accountDao;

	@GetMapping("/entry-status")
	public ResponseEntity<ApiResponse<?>> entryStatus(
			@AuthenticationPrincipal MemberDetails memberDetails,
			@RequestParam("product_no") long productNo
	) {
		if (memberDetails == null) {
			throw new ProductJoinUnauthorizedException();
		}

		ProductDetailViewDto product = productViewService.getProductDetail(productNo);

		if (product == null) {
			throw new IllegalArgumentException("상품 정보를 찾을 수 없습니다.");
		}

		BankMemberDto member = bankMemberService.getMemberInfo(memberDetails.getUsername());

		if (member == null) {
			throw new ProductJoinUnauthorizedException();
		}

		Long activeAccountNo = accountDao.findActiveAccountNoByMemberNo(member.getMember_no());
		String memberStatus = member.getMember_status();
		boolean regularMember = "REGULAR".equals(memberStatus);
		boolean hasActiveAccount = activeAccountNo != null;
		boolean accountRequired = !regularMember || !hasActiveAccount;
		boolean joinableProduct = "SALE".equals(product.getProduct_status())
				&& "Y".equalsIgnoreCase(product.getMobile_join_yn());
		boolean canEnterJoin = regularMember && hasActiveAccount && joinableProduct;

		return ResponseEntity.ok(ApiResponse.ok(ProductJoinEntryStatusDto.builder()
				.memberNo(member.getMember_no())
				.memberStatus(memberStatus)
				.regularMember(regularMember)
				.hasActiveAccount(hasActiveAccount)
				.accountRequired(accountRequired)
				.activeAccountNo(activeAccountNo)
				.productNo(product.getProduct_no())
				.productName(product.getProduct_name())
				.productType(product.getProduct_type())
				.joinableProduct(joinableProduct)
				.canEnterJoin(canEnterJoin)
				.message(resolveEntryMessage(accountRequired, joinableProduct))
				.build()));
	}

	@PostMapping("/start")
	public ResponseEntity<ApiResponse<?>> start(
			@AuthenticationPrincipal MemberDetails memberDetails,
			@RequestBody StartRequest request
	) {
		Long productNo = request == null ? null : request.productNo();

		if (productNo == null) {
			throw new IllegalArgumentException("상품 번호가 필요합니다.");
		}

		return ResponseEntity.ok(ApiResponse.ok(productSalesService.startJoin(memberNo(memberDetails), productNo)));
	}

	@GetMapping("/status")
	public ResponseEntity<ApiResponse<?>> status(
			@AuthenticationPrincipal MemberDetails memberDetails,
			@RequestParam("product_no") long productNo
	) {
		return ResponseEntity.ok(ApiResponse.ok(productSalesService.getDraftStatus(memberNo(memberDetails), productNo)));
	}

	@PostMapping("/terms")
	public ResponseEntity<ApiResponse<?>> terms(
			@AuthenticationPrincipal MemberDetails memberDetails,
			@RequestBody TermsRequest request
	) {
		if (request == null || request.subscriptionNo() == null) {
			throw new IllegalArgumentException("가입 진행 번호가 필요합니다.");
		}

		if (request.subscriptionAmount() == null || request.subscriptionMonths() == null) {
			throw new IllegalArgumentException("가입 금액과 기간이 필요합니다.");
		}

		return ResponseEntity.ok(ApiResponse.ok(productSalesService.saveTerms(
				memberNo(memberDetails),
				request.subscriptionNo(),
				request.subscriptionAmount(),
				request.subscriptionMonths(),
				request.isRequiredTermsAgreed(),
				request.isOptionalTermsAgreed()
		)));
	}

	@PostMapping("/complete")
	public ResponseEntity<ApiResponse<?>> complete(
			@AuthenticationPrincipal MemberDetails memberDetails,
			@RequestBody CompleteRequest request
	) {
		if (request == null || request.subscriptionNo() == null) {
			throw new IllegalArgumentException("가입 진행 번호가 필요합니다.");
		}

		return ResponseEntity.ok(ApiResponse.ok(productSalesService.completeJoin(
				memberNo(memberDetails),
				request.subscriptionNo(),
				request.accountPurpose()
		)));
	}

	private long memberNo(MemberDetails memberDetails) {
		if (memberDetails == null) {
			throw new ProductJoinUnauthorizedException();
		}

		return memberDetails.getPk();
	}

	private String resolveEntryMessage(boolean accountRequired, boolean joinableProduct) {
		if (!joinableProduct) {
			return "모바일 가입이 가능한 판매중 상품만 가입할 수 있습니다.";
		}

		if (accountRequired) {
			return "상품 가입을 위해서는 입출금 계좌 개설이 먼저 필요합니다.";
		}

		return "상품 가입 진입이 가능합니다.";
	}

	@ExceptionHandler(ProductJoinUnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnauthorized() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.fail("로그인이 필요합니다."));
	}

	@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException e) {
		return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
	}

	private static class ProductJoinUnauthorizedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
