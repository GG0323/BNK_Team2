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
import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.product.ProductJoinRequests.CompleteRequest;
import com.example.bnk.dto.product.ProductJoinRequests.StartRequest;
import com.example.bnk.dto.product.ProductJoinRequests.TermsRequest;
import com.example.bnk.service.product.ProductSalesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products/join")
@RequiredArgsConstructor
public class ProductJoinApiController {

	private final ProductSalesService productSalesService;

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
