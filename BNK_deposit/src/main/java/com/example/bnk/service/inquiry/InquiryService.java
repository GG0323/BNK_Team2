package com.example.bnk.service.inquiry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.inquiry.IInquiryDao;
import com.example.bnk.dao.inquiry.IInquiryMsgDao;
import com.example.bnk.dto.inquiry.InquiryDto;
import com.example.bnk.dto.inquiry.InquiryMsgListDto;

@Service
public class InquiryService {

	@Autowired
	private IInquiryDao inquiryDao;
	@Autowired
	private IInquiryMsgDao inquiryMsgDao;
	
	//인서트함수
	@Transactional
	public void insertInquiry(String inquiryCategory, String inquiryTitle, String msgContent, long uesrpk, String userid) {
		
		InquiryDto dto = new InquiryDto();
		dto.setInquiry_category(inquiryCategory);
	    dto.setInquiry_title(inquiryTitle);
		dto.setBank_member_no(uesrpk);
		
		// 인서트 함수 호출
		inquiryDao.insertInquiry(dto); // 여기서 selectKey 로 inquiry_no 를 세팅해준다.
		System.out.println("리터닝 값 : " + dto.getInquiry_no());
		
		
		int result = inquiryMsgDao.insertMsg( dto.getInquiry_no() , "USER" , userid , msgContent);
		System.out.println("결과 성공~~" + result);
	}
	
	// 내가 작성한 문의 리스트 
	public List<InquiryDto> callList(long uesrpk) {
		
		List<InquiryDto> list = inquiryDao.callList(uesrpk);
		
		return list;
	}
	// 문의사항 디테일 
	public InquiryMsgListDto callMsg(long inquiryNo) {
		
		InquiryMsgListDto dto = new  InquiryMsgListDto();
		
		InquiryDto inquiry = inquiryDao.callDetail(inquiryNo);
		System.out.println(inquiry.toString());
		// 메세지 헤더
		dto.setInquiry_no(inquiryNo);
		dto.setInquiry_category(inquiry.getInquiry_category());
		dto.setInquiry_status(inquiry.getInquiry_status());
		dto.setInquiry_title(inquiry.getInquiry_title());
		
		// 메세지 리스트 저장
		dto.setMsgDtoList(inquiryMsgDao.callMsg(inquiryNo));
		
		return dto;
	}
	// 추가 문의 사항 작성
	public void addMsg(long inquiryNo, String senderType, String username, String msgContent) {
		System.out.println("추가 문의 작성");
		inquiryMsgDao.insertMsg(inquiryNo, senderType, username, msgContent);
	}
	
	
	
	
	
	//========================================================직원이 답변하기위한 부분
	public List<InquiryDto> waitingAnswers() {
		System.out.println("모든 문의 사항 조회");
		List<InquiryDto> dtoList = inquiryDao.waitingAnswers();
		return dtoList;
	}

	public void updateStatus(long inquiryNo, String status) {
		System.out.println("상태 변경");
		inquiryDao.updateStatus(inquiryNo, status);
	}

	public void postingAnswer(long inquiryNo, String senderType, String username , String msgContent) {
		System.out.println("답변 등록");
		inquiryMsgDao.insertMsg(inquiryNo, senderType, username, msgContent);
	}

	
	
	
	
	
	
	
}
