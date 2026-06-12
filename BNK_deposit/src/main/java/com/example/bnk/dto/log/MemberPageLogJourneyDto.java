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
public class MemberPageLogJourneyDto {
	
	
    private long log_no;
    private String session_id;
    private Long member_no;          // 비로그인 구간이면 null
    private int step_no;             // 세션 내 이동 순번 (1부터)
    private String request_url;
    private String request_method;
    private Integer http_status;
    private String referer;
    private LocalDateTime accessed_at;
    private Long dwell_sec;          // 이 페이지 체류 시간(초). 세션 마지막 페이지는 null

}
