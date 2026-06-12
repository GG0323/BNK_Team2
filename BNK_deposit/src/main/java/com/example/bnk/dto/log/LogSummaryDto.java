package com.example.bnk.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LogSummaryDto {
	
	private long total_view;              // 총 페이지뷰
    private long total_session;           // 총 방문(세션) 수
    private long member_cnt;              // 방문한 회원 수 (로그인 기준)
    private Double avg_pages_per_session; // 방문당 평균 페이지 수

}
