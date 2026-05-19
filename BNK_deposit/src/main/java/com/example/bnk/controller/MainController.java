package com.example.bnk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
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
	
	// 관리자 - 제안서 보관함
	@GetMapping("/ppsPage")
	public String goProposalPage() {
		return "manager/proposal";
	}

}
