package com.example.bnk.dto.log;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LogStatResponseDto {
	
	private LogSummaryDto summary;
    private List<LogPageStatSummaryDto> page_stats;
    private List<LogDailyStatDto> daily_stats;
    private List<LogTransitionStatDto> transitions;
    private List<PersonaProductLogStatDto> persona_products;

    public LogStatResponseDto(
    		LogSummaryDto summary, 
    		List<LogPageStatSummaryDto> page_stats,
            List<LogDailyStatDto> daily_stats, 
            List<LogTransitionStatDto> transitions,
            List<PersonaProductLogStatDto> persona_products
            ) {
        this.summary = summary;
        this.page_stats = page_stats;
        this.daily_stats = daily_stats;
        this.transitions = transitions;
        this.persona_products = persona_products;
    }
	
	
}
