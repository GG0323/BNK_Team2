package com.example.bnk.dto.common;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor							// 금융 용어 사전
public class FinanceDictionaryDto {
	private long dictionary_no;				// 용어 번호
	private String dictionary_nm;			// 용어명
	private String dictionary_content;		// 용어 설명
	private String dictionary_category;		// 카테고리
	private long view_count;				// 조회수
	private LocalDate created_at;			// 등록일
	private LocalDate updated_at;			// 수정일
}
