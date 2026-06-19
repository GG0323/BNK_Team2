package com.example.bnk.dto.inquiry;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FaqCandidateDto {
	
    private Long   candidateNo;     // CANDIDATE_NO
    private String repQuestion;     // REP_QUESTION  (대표 질문)
    private String category;        // CATEGORY
    private int    inquiryCount;    // INQUIRY_COUNT (누적 문의 건수)
    private Double similarity;      // SIMILARITY    (참고용)
    private String status;          // STATUS        (대기/승인/반려)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
	
}
