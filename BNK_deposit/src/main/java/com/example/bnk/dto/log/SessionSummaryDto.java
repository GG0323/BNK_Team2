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
public class SessionSummaryDto {
	// 모든 로그를 세션 id별로 묶음
	
    private String session_id;
    private Long member_no;            // 방문 중 로그인했다면 회원번호, 끝까지 비로그인이면 null
    private LocalDateTime start_at;    // 방문 시작 (첫 요청 시각)
    private LocalDateTime end_at;      // 방문 종료 (마지막 요청 시각)
    private long page_cnt;             // 본 페이지 수
    private Long duration_sec;         // 방문 소요 시간(초) = 종료 - 시작
    private String first_url;          // 진입 페이지
    private String last_url;           // 이탈(마지막) 페이지
}
