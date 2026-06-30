package com.example.bnk.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	@Bean("fastApiRestTemplate")
	public RestTemplate fastApiRestTemplate(
			@Value("${fastapi.connect-timeout-ms:10000}") long connectTimeoutMs,
			@Value("${fastapi.read-timeout-ms:120000}") long readTimeoutMs
	) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
		return new RestTemplate(requestFactory);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOriginPatterns(
						"http://localhost:*",
						"https://localhost:*",
						"http://127.0.0.1:*",
						"https://127.0.0.1:*",
						"http://192.168.*.*:*",
						"https://192.168.*.*:*"
				)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true);
	}
	
	
	
	
	
}
