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
	
	
	private long logNo;
    private String sessionId;      // 여정 추적 UUID (로그인 전후 동일 값 유지)
    private Long memberNo;         // 비로그인 상태면 null
    private String requestUrl;
    private String requestMethod;
    private Integer httpStatus;
    private String requestIp;
    private String userAgent;
    private String referer;
    private Long durationMs;       // [예약] 프런트엔드 beacon 측정값
    private LocalDateTime accessedAt;

}
