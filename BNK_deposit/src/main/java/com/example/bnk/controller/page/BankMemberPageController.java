package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 회원 마이페이지 관련 "화면(View) 이동"만 담당하는 컨트롤러.
 *
 * 완전 분리 원칙:
 *  - 이 컨트롤러는 어떤 데이터도 조회하지 않는다. (Service / DAO 의존성 없음)
 *  - 단지 비어 있는 Thymeleaf 템플릿(껍데기 HTML) 경로만 반환한다.
 *  - 화면에 표시할 실제 데이터는 페이지 로드 후 JS 가 BankMemberApiController(@RestController)를
 *    fetch 로 호출해서 JSON 으로 받아온 뒤 그린다.
 *
 * 이렇게 하면 동일한 API 를 웹 브라우저와 모바일 앱이 모두 재사용할 수 있다.
 */
@Controller
public class BankMemberPageController {

	// 마이페이지
	@GetMapping("/mypage")
	public String mypage() {
		return "member/mypage";
	}

	// 내 정보 조회
	@GetMapping("/myinfo")
	public String myinfo() {
		return "member/myinfo";
	}

	// 내 정보 수정
	@GetMapping("/myinfo/edit")
	public String myinfoEdit() {
		return "member/myinfo_edit";
	}

	// 계좌 목록 조회
	@GetMapping("/myaccounts")
	public String myaccounts() {
		return "member/myaccounts";
	}

	// 계좌 상세 / 거래내역 조회
	// 기존에는 accountNo 가 없으면 서버에서 redirect 했지만,
	// 완전 분리 구조에서는 페이지는 항상 열어주고
	// accountNo 유효성 및 안내 처리는 클라이언트(JS)가 담당한다.
	@GetMapping("/myhistory")
	public String myhistory() {
		return "member/myhistory";
	}

	// 가입 상품 내역 조회
	@GetMapping("/myproducts")
	public String myproducts() {
		return "member/myproducts";
	}
	
	// 휴면 계정 해제 화면
	@GetMapping("/dormant/release")
	public String dormantRelease() {
	    return "member/dormant_release";
	}

	// 가입상품 상세 화면
	@GetMapping("/myproducts/detail")
	public String myProductsDetail() {
		return "member/myproducts_details";
	}
}