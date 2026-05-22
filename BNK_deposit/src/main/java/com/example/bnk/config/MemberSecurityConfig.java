package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.bnk.auth.EmployeeLoginSuccessHandler;
import com.example.bnk.auth.MemberDetailsService;
import com.example.bnk.auth.MemberLoginSuccessHandler;
import com.example.bnk.utils.JwtUtil;

@Configuration
@EnableWebSecurity
public class MemberSecurityConfig {
	
	private final MemberDetailsService memberDetailsService;
	private final JwtUtil jwtUtil;
	
	
	public MemberSecurityConfig(MemberDetailsService memberDetailsService, JwtUtil jwtUtil) {
		this.memberDetailsService = memberDetailsService;
		this.jwtUtil = jwtUtil;
	}
	
	@Bean	@Order(1)
	SecurityFilterChain memberFilterChain(HttpSecurity http) {
		
		// 권한별 제어
		http.userDetailsService(memberDetailsService)
			.securityMatcher("/member/**", "/loginPage", "/signupPage", "/api/member/**")
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()
		);
		
		// 회원 로그인 설정
		http.formLogin(member ->
			member.loginPage("/loginPage")
			.loginProcessingUrl("/member/login")
			.successHandler(new MemberLoginSuccessHandler(jwtUtil))
			.failureUrl("/loginPage?message=fail")
			.passwordParameter("password_hash")
			.usernameParameter("login_id")
		);
		
		return http.build();
	}
	
	

}
