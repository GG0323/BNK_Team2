package com.example.bnk.dao.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.inquiry.InquiryDto;
import com.example.bnk.dto.inquiry.InquiryMsgDto;

@Mapper
public interface IInquiryMsgDao {
	// 메세지 등록
	int insertMsg(
			@Param("inquiryNo") long inquiryNo,
			@Param("senderType") String senderType,
			@Param("senderId") String senderId,
			@Param("msgContent") String msgContent);
	
	// 문의 사항 대화 목록 불러오기
	List<InquiryMsgDto> callMsg(@Param("inquiryNo") long inquiryNo);
	
	

	
	
	
}
