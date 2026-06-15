package com.example.bnk.dto.log;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SessionListResponseDto {
	private final List<SessionSummaryDto> sessions;
    private final int totalCount;
    private final int totalPages;
    private final int page;
    
    
    public SessionListResponseDto(List<SessionSummaryDto> sessions, int totalCount, int totalPages, int page) {
        this.sessions = sessions;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.page = page;
    }

}
