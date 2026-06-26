package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.example.bnk.auth.JwtAuthFilter;
import com.example.bnk.auth.MemberDetailsService;
import com.example.bnk.auth.MemberLoginSuccessHandler;
import com.example.bnk.auth.SecurityLoginFailHandler;
import com.example.bnk.service.member.BankMemberService;

import com.example.bnk.utils.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class MemberSecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(MemberSecurityConfig.class);
	
	private final MemberDetailsService memberDetailsService;
	private final BankMemberService bankMemberService;
	private final JwtUtil jwtUtil;
	private final JwtAuthFilter jwtAuthFilter;
	
	public MemberSecurityConfig(
	        MemberDetailsService memberDetailsService,
	        JwtUtil jwtUtil,
	        BankMemberService bankMemberService,
	        JwtAuthFilter jwtAuthFilter
	) {
	    this.memberDetailsService = memberDetailsService;
	    this.jwtUtil = jwtUtil;
	    this.bankMemberService = bankMemberService;
	    this.jwtAuthFilter = jwtAuthFilter;
	}
	
	@Bean	@Order(1)
	SecurityFilterChain memberFilterChain(HttpSecurity http) {
		
		// 모바일에서 로그인할 때 csrf 토큰을 발급 받을 수 없으므로 예외처리용
		http.csrf(csrf -> csrf.ignoringRequestMatchers(
				PathPatternRequestMatcher.pathPattern("/member/login"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/member/accounts/open"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.DELETE, "/api/member/accounts/open"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/member/accounts/open/**"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.PUT, "/api/member/accounts/open/**"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.DELETE, "/api/member/accounts/open/**"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/products/join/**"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.PUT, "/api/products/join/**"),
				PathPatternRequestMatcher.pathPattern(HttpMethod.DELETE, "/api/products/join/**")
		));
		http.cors(Customizer.withDefaults());
		http.exceptionHandling(exception -> exception
				.authenticationEntryPoint(memberApiAuthenticationEntryPoint())
				.accessDeniedHandler(memberApiAccessDeniedHandler())
		);
		
		// 권한별 제어
		http.userDetailsService(memberDetailsService)
			.securityMatcher(
					"/member/**", "/loginPage", "/signupPage",
					"/api/member/**", "/dormant/**", "/api/dormant/**",
					"/api/products/member", "/api/products/member/**",
					"/api/products/join/**"
			)
			.authorizeHttpRequests(auth -> auth
					.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/member/accounts/open").hasAuthority("MEMBER")
					.requestMatchers(HttpMethod.DELETE, "/api/member/accounts/open").hasAuthority("MEMBER")
					.requestMatchers("/api/member/accounts/open/**").hasAuthority("MEMBER")
					.requestMatchers(HttpMethod.GET,
							"/api/products/member",
							"/api/products/member/search",
							"/api/products/member/detail").hasAuthority("MEMBER")
					.requestMatchers("/api/products/join/**").hasAuthority("MEMBER")
					.anyRequest().permitAll()
		);

		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		// 회원 로그인 설정
		http.formLogin(member ->
			member.loginPage("/loginPage")
			.loginProcessingUrl("/member/login")
			.successHandler(new MemberLoginSuccessHandler(jwtUtil, bankMemberService))
			.failureHandler(new SecurityLoginFailHandler())
		);
		
		// 회원 로그아웃 설정
		http.logout(logout -> logout
				.logoutUrl("/member/logout")
				.logoutSuccessHandler((request, response, auth) -> {
					Cookie cookie = new Cookie("bnk_token", null);
					cookie.setPath("/");
					cookie.setHttpOnly(true);
					cookie.setMaxAge(0);
					response.addCookie(cookie);
		
					response.sendRedirect("/loginPage?message=logout");
				})
				.invalidateHttpSession(true)
				.clearAuthentication(true)
		);
		
		return http.build();
	}

	private AuthenticationEntryPoint memberApiAuthenticationEntryPoint() {
		return (request, response, authException) -> {
			if (request.getRequestURI().startsWith("/api/")) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
				return;
			}

			response.sendRedirect("/loginPage");
		};
	}

	private AccessDeniedHandler memberApiAccessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			String path = request.getRequestURI();
			boolean accountOpenRequest = "/api/member/accounts/open".equals(path);
			boolean productJoinRequest = path.startsWith("/api/products/join/");

			if (accountOpenRequest || productJoinRequest) {
				Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
						.getContext()
						.getAuthentication();
				boolean csrfDenied = accessDeniedException instanceof MissingCsrfTokenException
						|| accessDeniedException instanceof InvalidCsrfTokenException;

				log.warn(
						"member api access denied: chain=memberFilterChain, path={}, method={}, csrfDenied={}, exception={}, authenticationExists={}, principalType={}, authorities={}",
						path,
						request.getMethod(),
						csrfDenied,
						accessDeniedException.getClass().getSimpleName(),
						authentication != null,
						authentication == null ? null : authentication.getPrincipal().getClass().getSimpleName(),
						authentication == null ? null : authentication.getAuthorities()
				);
			}

			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
		};
	}

	@Bean
	FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
		FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}
}
