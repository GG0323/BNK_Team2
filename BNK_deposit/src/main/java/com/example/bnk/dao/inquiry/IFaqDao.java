package com.example.bnk.dao.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.inquiry.FaqDto;

@Mapper
public interface IFaqDao {
	
	// faq 불러오기
	public List<FaqDto> getFAQ();

}
