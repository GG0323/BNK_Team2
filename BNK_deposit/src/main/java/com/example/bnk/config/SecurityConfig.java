package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.bnk.auth.EmployeeLoginSuccessHandler;
import com.example.bnk.auth.MemberLoginSuccessHandler;
import com.example.bnk.utils.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final JwtUtil jwtUtil;
	
	public SecurityConfig(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) {
		
		// 권한별 제어
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/images/**", "/**").permitAll()
		);
		
		
		// 직원 로그인 설정
		http.formLogin(employee ->
			employee.loginPage("/employee/loginPage") // 페이지
			.loginProcessingUrl("/employee/login") // 프로세스 html의 요청
			.successHandler(new EmployeeLoginSuccessHandler(jwtUtil))
			.failureUrl("/employee/loginPage?message=fail")
			.passwordParameter("password_hash")
			.usernameParameter("login_id")
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
	
	@Bean
	BCryptPasswordEncoder passwordEncode() {
		return new BCryptPasswordEncoder();
	}

}
