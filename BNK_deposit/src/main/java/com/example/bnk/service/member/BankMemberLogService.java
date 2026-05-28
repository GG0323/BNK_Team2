package com.example.bnk.service.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IMemberLogDao;
import com.example.bnk.dto.member.BankMemberLogDto;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class BankMemberLogService {
	
	@Autowired
	private HttpServletRequest request; // 스프링이 현재 요청의 객체를 주입한다.
	@Autowired
	private IMemberLogDao logDao;
	
	
	//컨트롤러에서 어트리뷰트에 Dto저장하기
	public BankMemberLogDto build(
			long member_no, String requested_page
			) {
		BankMemberLogDto dto = new BankMemberLogDto();
		
		dto.setMember_no(member_no);
		dto.setRequested_page(requested_page);
		dto.setRequest_method(request.getMethod());
		dto.setRequest_url(request.getRequestURI());
		
		request.setAttribute("memberLogDto", dto);
		return dto;
	}
	
	// @AuthenticationPrincipal String username
	public long findByUserID(String userid) {
		
		System.out.println("로그인 한 유저의 ID:" +userid);
		long memberpk = logDao.findByUserID(userid);
		System.out.println("로그인 한 유저의 PK: "+memberpk);
		
		return memberpk;
	}
	
	
	// 로그 인서트하기
	public void log(BankMemberLogDto insertDto) {
		
		int result = logDao.insertLog(insertDto);
		
		if(result == 1) System.out.println("로그 저장 성공 ");
		
	}
	
	// 모든 로그 리스트 
	public List<BankMemberLogDto> allLog(){
		
		List<BankMemberLogDto> list = logDao.allLog();
		
		return list;
	}
	
	
	
}
