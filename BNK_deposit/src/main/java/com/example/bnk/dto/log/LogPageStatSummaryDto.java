package com.example.bnk.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogPageStatSummaryDto {
	
	
    private String request_url;
    private long view_cnt;            // 페이지뷰 수
    private long session_cnt;         // 이 페이지를 본 방문(세션) 수
    private Double avg_dwell_sec;     // 평균 체류 시간(초). 측정 불가면 null
    private Double median_dwell_sec;  // 중앙값 체류 시간(초). 이상치 영향이 적어 평균과 함께 본다


}
