package com.example.bnk.auth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.bnk.utils.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MemberLoginSuccessHandler implements AuthenticationSuccessHandler{
	
	private final JwtUtil jwtUtil;
	
	public MemberLoginSuccessHandler(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		// 로그인에 성공한 사용자 정보 추출
		MemberDetails memberDetails = (MemberDetails)authentication.getPrincipal();
				
		// JWT 토큰 생성
		String token = jwtUtil.generateToken(memberDetails.getUsername(), "ROLE_MEMBER");
		
		// Cookie에 토큰 저장
		Cookie cookie = new Cookie("bnk_token", token);
		cookie.setPath("/");
		cookie.setHttpOnly(true);		// 자바스크립트에서 쿠키를 못 읽게하는 옵션
		
		response.addCookie(cookie);
		
		// 결과 전송
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().write("{\"result\" : \"success\", \"role\" : \"ROLE_MEMBER\"}");
		
	}

}
