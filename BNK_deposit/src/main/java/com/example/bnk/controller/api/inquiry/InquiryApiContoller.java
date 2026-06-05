package com.example.bnk.controller.api.inquiry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.inquiry.FaqDto;
import com.example.bnk.service.inquiry.FaqService;

@RestController
@RequestMapping("/api/inquiry")
public class InquiryApiContoller {
	
	@Autowired
	private FaqService faqService;
	
	// faq 페이지 진입시 faq 목록을 불러오는 컨트롤러
	@GetMapping("/faq")
	public List<FaqDto> faqList(){
		
		List<FaqDto> faqlist = faqService.getFAQ();
		
		return faqlist;
	}
	
	
	
}
