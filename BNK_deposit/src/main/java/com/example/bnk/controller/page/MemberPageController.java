package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
	
	

}
