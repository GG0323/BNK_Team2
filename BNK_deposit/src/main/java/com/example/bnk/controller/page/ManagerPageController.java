package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagerPageController {
	
	/* 관리자 페이지
	------------------------------------------------------------------------------------*/
	
	// 관리자 메인페이지
	@GetMapping("/mngMain")
	public String goManagerMainPage() {
		return "manager/managerMain";
	}
	
	// 관리자 - 제안서 관리 페이지 이동
	@GetMapping("/ppsPage")
	public String goProposalPage() {
		return "manager/proposal";
	}
	
	// 관리자 - 상품 관리 페이지 이동
	@GetMapping("/prdPage")
	public String goProductPage() {
		return "manager/product";
	}
	
	// 관리자 - 직원 관리 페이지 이동
	@GetMapping("/stfPage")
	public String goManagerPage() {
		return "manager/staff";
	}
	
	// 관리자 - 회원 관리 페이지 이동
	@GetMapping("/mmbPage")
	public String goMemberPage() {
		return "manager/member";
	}
	
	// 관리자 - 커뮤니티 공지 작성 페이지
	@GetMapping("/comuPage")
	public String writeComuPage() {
		return "manager/comu";
	}
	
}
