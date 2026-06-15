package com.example.bnk.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberPageLogSearchDto {
	// 로그 검색 조건을 설정하는 Dto
	
	private Long memberNo;     // 회원번호 (null 이면 전체)
    private String fromDate;   // 조회 시작일 yyyy-MM-dd (null 이면 제한 없음)
    private String toDate;     // 조회 종료일 yyyy-MM-dd (해당 일자 포함)
    private String url;        // URL 부분 일치 검색어

    private int page = 1;      // 현재 페이지 (1부터)
    private int size = 20;     // 페이지당 행 수

    /** Oracle OFFSET 절에 바로 쓰는 값 */
    public int getOffset() {
        return (Math.max(page, 1) - 1) * size;
    }
    
    
}
