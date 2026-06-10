package com.example.bnk.service.inquiry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.inquiry.IFaqDao;
import com.example.bnk.dto.inquiry.FaqDto;

@Service
public class FaqService {
	// i를 불러온다.
	@Autowired
	private IFaqDao faqDao;
	
	
	public List<FaqDto> getFAQ() {
		
		List<FaqDto> faqlist = faqDao.getFAQ();
		
		return faqlist;
	}

}
