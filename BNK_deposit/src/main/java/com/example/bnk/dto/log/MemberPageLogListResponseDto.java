package com.example.bnk.dto.log;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
public class MemberPageLogListResponseDto {
	
	private final List<MemberPageLogDto> logs;
    private final int totalCount;
    private final int totalPages;
    private final int page;

    public MemberPageLogListResponseDto(List<MemberPageLogDto> logs, int totalCount, int totalPages, int page) {
        this.logs = logs;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.page = page;
    }
    
}
