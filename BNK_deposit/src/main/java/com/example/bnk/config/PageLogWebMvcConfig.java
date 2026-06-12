package com.example.bnk.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.bnk.filter.MemberPageLogInterceptor;

@Configuration
public class PageLogWebMvcConfig implements WebMvcConfigurer {
	
	@Autowired
    private MemberPageLogInterceptor memberPageLogInterceptor;
    
    // 만들어낸 인터셉터를 프로그램에 등록하고 로그로 수집하지 않응 예외 항목을 정의 한다.
  

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberPageLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                		"/employee/**", 
                        "/css/**", "/js/**", "/images/**", "/img/**", "/fonts/**",
                        "/favicon.ico",
                        "/error",                 // 스프링 기본 에러 포워딩 (상태코드는 원 요청에 이미 기록됨)
                        "/api/**",                // 데이터 조회용 AJAX — 섞이면 체류 시간 계산이 왜곡됨
                        "/actuator/**"            // 모니터링 엔드포인트 (사용 시)
                        // 프로젝트 상황에 맞게 추가: 헬스체크, 폴링성 AJAX 등
                );
    }
	
	
}
