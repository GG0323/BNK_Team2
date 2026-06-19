package com.example.bnk.auth;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.bnk.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
	
	private static final String TOKEN_COOKIE_NAME = "bnk_token";
	
	private final JwtUtil jwtUtil;
	private final EmployeeDetailsService employeeDetailsService;
	private final MemberDetailsService memberDetailsService;
	
	public JwtAuthFilter(
			JwtUtil jwtUtil,
			EmployeeDetailsService employeeDetailsService,
			MemberDetailsService memberDetailsService
	) {
		this.jwtUtil = jwtUtil;
		this.employeeDetailsService = employeeDetailsService;
		this.memberDetailsService = memberDetailsService;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String token = findTokenFromCookies(request, TOKEN_COOKIE_NAME);
		
		if(token == null) {
			filterChain.doFilter(request, response);
			return;
		}
		
		if(jwtUtil.isVaild(token)) {
			setAuthentication(token);
			filterChain.doFilter(request, response);
			return;
		}
		
		// 쿠키는 있는데 만료/위조/손상 등 유효하지 않은 토큰이면 로그아웃 처리
		clearAuthentication(request, response);
		
		if(isPublicRequest(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		sendAuthError(request, response);
	}
	
	private void setAuthentication(String token) {
		String username = jwtUtil.getUsername(token);
		String role = jwtUtil.getRole(token);
		UserDetails userDetails = "ROLE_MEMBER".equals(role)
				? memberDetailsService.loadUserByUsername(username)
				: employeeDetailsService.loadUserByUsername(username);
		
		Authentication auth = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
		);
		
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
	
	private String findTokenFromCookies(HttpServletRequest request, String tokenName) {
		Cookie[] cookies = request.getCookies();
		
		if(cookies == null) return null;
		
		for(Cookie ck : cookies) {
			if(tokenName.equals(ck.getName())) {
				return ck.getValue();
			}
		}
		
		return null;
	}
	
	// 인증 정보와 JWT 쿠키 제거
	private void clearAuthentication(HttpServletRequest request, HttpServletResponse response) {
		SecurityContextHolder.clearContext();
		
		HttpSession session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
		
		Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
	
	private void sendAuthError(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String redirectUrl = resolveLoginUrl(request);
		
		if(isApiRequest(request)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json; charset=UTF-8");
			response.setHeader("X-Auth-Expired", "true");
			response.getWriter().write("{\"success\":false,\"message\":\"로그인이 만료되었습니다.\",\"redirectUrl\":\"" + redirectUrl + "\"}");
			return;
		}
		
		response.sendRedirect(redirectUrl + "?message=expired");
	}
	
	private boolean isApiRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String accept = request.getHeader("Accept");
		
		return uri.startsWith("/api/")
				|| (accept != null && accept.contains("application/json"));
	}
	
	// [JWT 자동 로그아웃 추가] 공개 요청은 쿠키만 지우고 통과
	private boolean isPublicRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		
		return uri.equals("/")
				|| uri.equals("/loginPage")
				|| uri.equals("/signupPage")
				|| uri.equals("/employee/toMain")
				|| uri.equals("/employee/login")
				|| uri.equals("/member/login")
				|| uri.startsWith("/css/")
				|| uri.startsWith("/js/")
				|| uri.startsWith("/images/")
				|| uri.startsWith("/products")
				|| uri.startsWith("/api/products")
				|| uri.startsWith("/api/signup/");
	}
	
	private String resolveLoginUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		
		if(uri.startsWith("/employee")
				|| uri.startsWith("/api/employee")
				|| uri.startsWith("/api/staff")
				|| uri.startsWith("/api/manager")
				|| uri.startsWith("/api/log/employee")) {
			return "/employee/toMain";
		}
		
		return "/loginPage";
	}

}
