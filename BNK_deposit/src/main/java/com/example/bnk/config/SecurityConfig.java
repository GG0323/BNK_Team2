package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.bnk.auth.EmployeeLoginSuccessHandler;
import com.example.bnk.auth.MemberLoginSuccessHandler;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.utils.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final JwtUtil jwtUtil;
	private final BankMemberService bankMemberService;
	
	public SecurityConfig(JwtUtil jwtUtil, BankMemberService bankMemberService) {
		this.jwtUtil = jwtUtil;
		this.bankMemberService = bankMemberService;
	}
	
	@Bean @Order(99)
	SecurityFilterChain filterChain(HttpSecurity http) {
		
		http.csrf(csrf -> csrf.disable());
		
		// 권한별 제어
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/fonts/**", "/favicon.ico").permitAll()
				.requestMatchers("/common/**", "/api/**", "/api/orchestrator/**", "/error").permitAll()
				.anyRequest().authenticated()
		);

		http.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					if (request.getRequestURI().startsWith("/api/")) {
						response.setStatus(401);
						response.setContentType(MediaType.APPLICATION_JSON_VALUE);
						response.setCharacterEncoding("UTF-8");
						response.getWriter().write("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
						return;
					}

					response.sendRedirect("/loginPage");
				})
		);
		
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
			.successHandler(new MemberLoginSuccessHandler(jwtUtil, bankMemberService))
			.failureUrl("/loginPage?message=fail")
			.passwordParameter("password_hash")
			.usernameParameter("login_id")
		);
		
		return http.build();
	}
	
	@Bean @Order(3)
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(request -> {
			org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
			config.setAllowedOriginPatterns(java.util.List.of("*"));
			config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
			config.setAllowedHeaders(java.util.List.of("*"));
			config.setAllowCredentials(true);
			return config;
		}));
		http.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll());
				
		http.csrf(csrf -> csrf.disable());

		// 권한별 제어
		http.securityMatcher(
					"/", "/loginPage", "/signupPage",
					"/products", "/products/**",
					"/api/products", "/api/products/search", "/api/products/mobile-qr-image",
					"/api/products/ai/recommend",
					"/api/orchestrator/**", "/api/ai/**", "/api/finance/**",
					"/error"
			)
			.authorizeHttpRequests(auth -> auth
					.requestMatchers("/", "/loginPage", "/signupPage", "/error").permitAll()
					.requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/products", "/api/products/search", "/api/products/mobile-qr-image").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/products/ai/recommend").permitAll()
					.requestMatchers("/api/orchestrator/**", "/api/ai/**", "/api/finance/**").permitAll()
					.anyRequest().authenticated()
		);
		
		return http.build();
	}
}
