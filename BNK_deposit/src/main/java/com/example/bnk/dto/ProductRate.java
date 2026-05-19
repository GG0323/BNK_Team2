package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor							// 상품 금리
public class ProductRate {
	private long rate_no;					// 상품 금리 PK
	private long product_no;				// 상품 FK
	private String rate_group;				// 금리 구분 ('MATURITY', 'AFTER_MATURITY', 'EARLY_TERMINATION')
	private String rate_label;				// 금리 구간/조건명
	private String annual_rate;				// 연이율
	private String annual_return_rate;		// 연수익률
	private String note;					// 비고
	private int display_order;				// 노출 순서
	private String use_yn;					// 사용 여부 ('Y','N')
	private LocalDate created_at;			// 생성일
	private LocalDate updated_at;			// 수정일
}
