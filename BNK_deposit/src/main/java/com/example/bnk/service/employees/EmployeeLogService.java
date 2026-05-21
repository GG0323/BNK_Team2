package com.example.bnk.service.employees;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.employee.IEmployeeLogDao;
import com.example.bnk.dto.employee.EmployeeLogDto;
import com.example.bnk.dto.employee.EmployeeLogInsertDto;
import com.example.bnk.dto.employee.EmployeeLogSelectDto;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class EmployeeLogService {
	
	@Autowired
	private HttpServletRequest request; // 스프링이 현재 요청의 객체를 주입한다.
	@Autowired
	private IEmployeeLogDao logDao;
	
	// 로그 인서트하기
	public void log(EmployeeLogInsertDto insertDto) {
		
		int result = logDao.insertLog(insertDto);
		
		if(result == 1) System.out.println("로그 저장 성공 ");
		
	}
	// 컨트롤러 단에서 HttpServletRequest의 어트리뷰트 영역에 Dto 저장하기
	public EmployeeLogInsertDto build(
			String action_type, String target_table, String target_pk, 
			String action_detail,String request_method, String request_url 
			) {
		EmployeeLogInsertDto insertDto = new EmployeeLogInsertDto(
				action_type, target_table, target_pk,
				action_detail, request_method, request_url);
		
		request.setAttribute("insertLogDto", insertDto);
		
		return insertDto;
	}
	
	// 모든 로그 리스트 
	public List<EmployeeLogDto> allLog(){
		
		List<EmployeeLogDto> list = logDao.allLog();
		
		for(EmployeeLogDto log : list) {
			System.out.println(log.toString());
		}
		
		return list;
	}
	// 검색 조건 로그 리스트
	public List<EmployeeLogDto> conditionList(EmployeeLogSelectDto selectDto) {
		
		List<EmployeeLogDto> conlist = logDao.conditionLog(selectDto);
		
		for(EmployeeLogDto log : conlist) {
			System.out.println(log.toString());
		}
		
		return conlist;
	}
	
	
	
}
