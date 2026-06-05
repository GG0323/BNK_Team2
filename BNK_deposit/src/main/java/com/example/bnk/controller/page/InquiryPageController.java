package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inquery")
public class InquiryPageController {
	
	
	
	// FAQ 페이지  /inquery/faqPage
	@GetMapping("/faqPage")
	public String faqPage() {
		return "inquiry/faqPage";
	}
	// 문의 사항 작성
	@GetMapping("/inquiryForm")
	public String inquiryForm() {
		return "inquiry/inquiryForm";
	}
	// 문의 사항 리스트
	@GetMapping("/inquiryList")
	public String inquiryList() {
		return "inquiry/inquiryList";
	}
	// 문의 사항 상세페이지
	@GetMapping("/inquiryDetail")
	public String inquiryDetail() {
		return "inquiry/inquiryDetail";
	}
	
	
	
	
}
