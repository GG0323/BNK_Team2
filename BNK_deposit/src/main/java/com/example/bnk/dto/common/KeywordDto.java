package com.example.bnk.dto.common;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor							// 키워드 사전
public class KeywordDto {
	private long keyword_id;				// 키워드 PK
	private String input_keyword;			// 사용자가 입력한 키워드
	private String normalized_keyword;		// 보정된 표준 키워드
	private String category_name;			// 검색 카테고리명
	private String keyword_type;			// 키워드 유형 | SYNONYM: 동의어 | TYPO: 오타 교정 | ABBR: 축약어 | RELATED: 연관 검색어
	private int keyword_priority;			// 추천 우선순위
	private String use_yn;					// 사용 여부
	private LocalDate created_at;			// 생성일
}
