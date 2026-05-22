package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.bnk.auth.EmployeeDetailsService;
import com.example.bnk.auth.SecurityLoginFailHandler;
import com.example.bnk.auth.EmployeeLoginSuccessHandler;
import com.example.bnk.auth.MemberLoginSuccessHandler;
import com.example.bnk.utils.JwtUtil;

@Configuration
@EnableWebSecurity
public class EmployeeSecurityConfig {
	
	private final EmployeeDetailsService employeeDetailsService;
	
	private final JwtUtil jwtUtil;
	
	public EmployeeSecurityConfig(EmployeeDetailsService employeeDetailsService, JwtUtil jwtUtil) {
		this.employeeDetailsService = employeeDetailsService;
		this.jwtUtil = jwtUtil;
	}
	
	@Bean	@Order(2)
	SecurityFilterChain employeeFilterChain(HttpSecurity http) {
		
		// 권한별 제어
		http.securityMatcher("/employee/**")
			.userDetailsService(employeeDetailsService)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()
		);
		
		
		// 직원 로그인 설정
		http.formLogin(employee ->
			employee.loginPage("/employee/loginPage")
			.loginProcessingUrl("/employee/login")
			.successHandler(new EmployeeLoginSuccessHandler(jwtUtil))
			.failureHandler(new SecurityLoginFailHandler())
			.passwordParameter("password_hash")
			.usernameParameter("login_id")
		);
			
		return http.build();
	}
}
