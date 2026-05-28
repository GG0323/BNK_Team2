package com.example.bnk.dto.employee;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmployeeLogInsertDto {

	private long employee_no;			// 작업 수행 직원 번호
	private String action_type;			// 수행 작업 유형
	private String target_table;		// 작업 대상 테이블
	private String select_key;			// 작업 대상 테이블의 PK
	private String action_detail;		// 작업 상세 내용
	private String request_method;		// 요청 메소드
	private String request_url;			// 요청 URL
	private String request_ip;			// 요청 IP
	private int response_status;		// 응답 상태 코드

	public EmployeeLogInsertDto (
			long employee_no, String action_type, String target_table, String select_key, 
			String action_detail,String request_method, String request_url 
			){
		this.employee_no = employee_no; // 하드코딩 임시 4번 >> auth에서 가져오기
		this.action_type = action_type;
		this.target_table = target_table;
		this.select_key = select_key;
		this.action_detail = action_detail;
		this.request_method = request_method;
		this.request_url = request_url;
		//this.request_ip = request_ip; 필터 단에서 구현
		//this.response_status = response_status; 필터 구현
	}
}
