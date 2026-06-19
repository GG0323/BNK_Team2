package com.example.bnk.dao.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.inquiry.FaqDto;

@Mapper
public interface IFaqDao {
	
	// faq 불러오기
	public List<FaqDto> getFAQ();
	
	// faq 등록하기
	int insertNewFaq(@Param("faqQuestion") String question, 
					 @Param("faqAnswer") String answer,
			         @Param("faqCategory") String faqCategory,
			         @Param("faqOrder") long faqOrder,
			         @Param("createdBy") long createdBy);
	

}
