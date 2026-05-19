package com.example.bnk.dto.product;

import lombok.Data;

@Data
// 승인된 상품 테이블(TMP)
public class ApprovedSuggestionDto {
	private long suggestion_no;		// 승인된 제안서 FK 이자 PK
}
