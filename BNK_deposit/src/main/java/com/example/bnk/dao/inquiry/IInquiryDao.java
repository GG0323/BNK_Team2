package com.example.bnk.dao.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.inquiry.InquiryDto;

@Mapper
public interface IInquiryDao {
	
	// 새로운 인서트, 리터닝 받아오기
	int insertInquiry(@Param("dto") InquiryDto inquiryDto );
	
	// 문의 사항 목록 불러오기
	List<InquiryDto> callList(@Param("bank_member_no") long uesrpk);
	
	// 문의 사항 머리 불러오기
	InquiryDto callDetail(@Param("inquiryNo") long inquiryNo);

	
	
	//=====================================================================
	// 직원용 전채 문의사항 확인 
	List<InquiryDto> waitingAnswers();
	
	void updateStatus(@Param("inquiryNo") long inquiryNo, @Param("inquiryStatus") String status);
	
	
	
}
