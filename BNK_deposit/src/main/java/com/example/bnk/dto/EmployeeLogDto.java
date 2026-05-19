package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor						// 직원로그
public class EmployeeLogDto {
	private long log_no;				// 로그 PK
	private long employee_no;			// 작업 수행 직원 번호
	private String action_type;			// 수행 작업 유형
	private String target_table;		// 작업 대상 테이블
	private String target_pk;			// 작업 대상 테이블의 PK
	private String action_detail;		// 작업 상세 내용
	private String request_method;		// 요청 메소드
	private String request_url;			// 요청 URL
	private String request_ip;			// 요청 IP
	private int response_status;		// 응답 상태 코드
	private LocalDate created_at;		// 작업 일시
}
