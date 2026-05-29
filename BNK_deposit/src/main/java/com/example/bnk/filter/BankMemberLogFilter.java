package com.example.bnk.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.bnk.dto.member.BankMemberLogDto;
import com.example.bnk.service.member.BankMemberLogService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class BankMemberLogFilter extends OncePerRequestFilter {
	
	@Autowired
	private BankMemberLogService logService;	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest  servletRequest  = (HttpServletRequest)  request;
	    HttpServletResponse servletResponse = (HttpServletResponse) response;
	    
	    // 요청넘기기
 		chain.doFilter(request, response);
	 	
 		try {
 			BankMemberLogDto logDto = (BankMemberLogDto) servletRequest.getAttribute("memberLogDto");
 			if(logDto == null) return;
 			
 			//요청 IP 저장
 			logDto.setRequest_ip(servletRequest.getRemoteAddr());
 			//로그 DB에 저장
 			logService.log(logDto);
 			
 		}catch (Exception e) {
 			System.out.println("멤버 로그 저장 실패: {}"+ e.getMessage());
 		}
		
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
	
	
}
