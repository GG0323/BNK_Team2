package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	BCryptPasswordEncoder passwordEncode() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean @Order(3)
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
		// 권한별 제어
		http.securityMatcher("/", "/loginPage", "/signupPage", "/products/**")
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()
		);
		
		return http.build();
	}
}