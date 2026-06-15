package com.example.bnk.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogDailyStatDto {
	private String stat_date;     // yyyy-MM-dd
    private long view_cnt;        // 그 날의 페이지뷰
    private long session_cnt;     // 그 날의 방문(세션) 수
}
