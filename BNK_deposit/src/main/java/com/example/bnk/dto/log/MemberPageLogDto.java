package com.example.bnk.dto.log;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberPageLogDto {
	
	
	private long log_no;
    private String session_id;      // 여정 추적 UUID (로그인 전후 동일 값 유지)
    private Long member_no;         // 비로그인 상태면 null
    private String request_url;
    private String request_method;
    private Integer http_status;
    private String request_ip;
    private String user_agent;
    private String referer;
    private Long duration_ms;       // [예약] 프런트엔드 beacon 측정값
    private LocalDateTime accessed_at;
    
    
    
    
}
