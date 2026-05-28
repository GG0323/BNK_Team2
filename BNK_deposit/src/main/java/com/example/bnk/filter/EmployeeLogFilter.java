package com.example.bnk.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.bnk.dao.employee.IEmployeeLogDao;
import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.employee.EmployeeLogInsertDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.employees.EmployeeLogService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class EmployeeLogFilter extends OncePerRequestFilter{
	
	@Autowired
	private EmployeeLogService LogService;	
	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest  servletRequest  = (HttpServletRequest)  request;
	    HttpServletResponse servletResponse = (HttpServletResponse) response;
		
		
		
		// 요청넘기기
		chain.doFilter(request, response);
		
		// 응답완료후
		try {
			
			EmployeeLogInsertDto insertDto = (EmployeeLogInsertDto) servletRequest.getAttribute("insertLogDto");
			if(insertDto == null) return; // build 함수가 호출된 적 없다면 리턴
			
			// 응답 status 저장
			insertDto.setResponse_status(servletResponse.getStatus()); 
			// 요청 IP 저장
			insertDto.setRequest_ip(servletRequest.getRemoteAddr()); // 또는 servletRequest.getHeader("X-Forwarded-For")
			// 직원 pk 저장 -> @AuthenticationPrincipal String username
			// empService.findByUsername(username);로 정보 받아오기 @Autowired
			// private EmployeeListService empService
			//EmployeeDto pk = empService.findByUsername(username);
			//pk.getEmployee_no()
			
			//insertDto.setEmployee_no(4);
			
			LogService.log(insertDto);
			
		}catch (Exception e) {
			System.out.println("[ActivityLogFilter] 로그 저장 실패: {}"+ e.getMessage());
		}
		
		
		
	}



	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
}
