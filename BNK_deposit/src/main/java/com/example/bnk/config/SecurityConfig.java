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
//				.requestMatchers("/mypage/**", "/myinfo/**", "/myinfo_edit/**", "/myaccounts/**", "/myhistory/**","/myproducts/**").hasAuthority("ROLE_MEMBER").anyRequest().permitAll()
		);
		
//		http.exceptionHandling(exception -> exception
//				.authenticationEntryPoint((request, response, authException) -> {
//					// 권한이 없으면 로그인 페이지로 튕겨냅니다. 
//	                // (?error=login_required 라는 꼬리표를 달아서 보냅니다)
//					response.sendRedirect("/loginPage?error=login_required");
//			})
//		);
		
		
		// 직원 로그인 설정
		http.formLogin(employee ->
			employee.loginPage("/employee/loginPage")
			.loginProcessingUrl("/employee/login")
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
