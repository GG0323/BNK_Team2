package com.example.bnk.dto.product;

import lombok.Data;

@Data
// 승인된 상품 테이블(TMP)
public class ApprovedSuggestionDto {
	private long suggestion_no;		// 승인된 제안서 FK 이자 PK
	private long rate_no;			// 금리
	private long terms_no;			// 약관
	private long description_no;	// 설명
	private long condition_no;		// 조건
}
