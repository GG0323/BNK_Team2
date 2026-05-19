package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberTrackingLogDto {
	// 회원 추적 LOG
	
	private long member_tracking_log_no; // 로그 PK
	private long member_no;              // 회원 번호 fk 없음
	private String requested_page;       // 요청 페이지
	private String request_method;       // 요청 메소드
	private String request_url;          // 요청 URL
	private String request_ip;           // 요청 IP
	private LocalDate accessed_at;       // 접근 시간 DEFAULT SYSDATE
}
