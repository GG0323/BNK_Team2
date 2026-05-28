package com.example.bnk.auth;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.utils.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MemberLoginSuccessHandler implements AuthenticationSuccessHandler{
	
	private final JwtUtil jwtUtil;
	private final BankMemberService bankMemberService;
	
	public MemberLoginSuccessHandler(JwtUtil jwtUtil, BankMemberService bankMemberService) {
		this.jwtUtil = jwtUtil;
		this.bankMemberService = bankMemberService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		String loginId = authentication.getName();
		BankMemberDto member = bankMemberService.getMemberInfo(loginId);
		
		if (member == null) {
		    response.sendRedirect("/loginPage?message=memberNotFound");
		    return;
		}

		if ("WITHDRAWN".equals(member.getMember_status())) {
		    SecurityContextHolder.clearContext();
		    request.getSession().invalidate();
		    response.sendRedirect("/loginPage?message=withdrawn");
		    return;
		}

		if ("DORMANT".equals(member.getMember_status())) {
		    request.getSession().setAttribute("DORMANT_LOGIN_ID", loginId);
		    response.sendRedirect("/member/dormant/release");
		    return;
		}
		
		LocalDateTime lastLoginAt = member.getLast_login_at();
		
		if (lastLoginAt != null && lastLoginAt.isBefore(LocalDateTime.now().minusDays(7))) {
		    bankMemberService.makeDormant(loginId);
		    request.getSession().setAttribute("DORMANT_LOGIN_ID", loginId);
		    response.sendRedirect("/member/dormant/release");
		    return;
		}
		

	    // 로그인 성공 시 마지막 접속 시간 갱신
	    bankMemberService.updateLastLoginAt(loginId);
		
		// 로그인에 성공한 사용자 정보 추출
		MemberDetails memberDetails = (MemberDetails)authentication.getPrincipal();
		
		// 마지막 로그인 일시 기록
		bankMemberService.updateLastLogin(memberDetails.getPk());
		
		// JWT 토큰 생성
		String token = jwtUtil.generateToken(memberDetails.getUsername(), "ROLE_MEMBER");
		
		// Cookie에 토큰 저장
		Cookie cookie = new Cookie("bnk_token", token);
		cookie.setPath("/");
		response.addCookie(cookie);
		
		// 로그인 성공 데이터 보내기
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().write("{ \"result\" : \"success\"}");
		
	}

}
