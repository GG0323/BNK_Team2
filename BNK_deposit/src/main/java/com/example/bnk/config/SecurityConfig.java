package com.example.bnk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	BCryptPasswordEncoder passwordEncode() {
		return new BCryptPasswordEncoder();
	}
	
	
	
}