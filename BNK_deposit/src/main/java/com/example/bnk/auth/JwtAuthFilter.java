package com.example.bnk.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.bnk.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
	
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
		
		// 쿠키에서 토큰 추출
		String token = this.getTokenFromCookie(request);
		
		// 토큰이 있고, 그 토큰이 유효하면
		if(token != null && jwtUtil.isVaild(token)) {
			String username = jwtUtil.getUsername(token);
			String role = jwtUtil.getRole(token);
			UserDetails userDetails;
			
			System.out.println(role);

			userDetails = "ROLE_MEMBER".equals(role)
					? memberDetailsService.loadUserByUsername(username)
					: employeeDetailsService.loadUserByUsername(username);
			
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		filterChain.doFilter(request, response);
	}
	
	// 쿠키에서 토큰 찾기
	private String getTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookie = request.getCookies();
		if(cookie == null) return null;
		for(Cookie ck : cookie) {
			if("bnk_token".equals(ck.getName())) {
				return ck.getValue();
			}
		}
		return null;
	}

}
