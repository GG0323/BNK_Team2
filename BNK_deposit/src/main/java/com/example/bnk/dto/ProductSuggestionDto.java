package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor								// 상품 제안서
public class ProductSuggestionDto {
	private long suggestion_no;					// 제안서 PK
	private long employee_no;					// 제안 직원 FK
	private long manager_employee_no;			// 승인 책임자 FK
	private String product_name;				// 제안 상품명
	private String suggestion_content;			// 제안 내용
	private double proposed_min_interest_rate;	// 제안 최저금리
	private double proposed_max_interest_rate;	// 제안 최고금리
	private String approval_status;				// 승인 상태
	private String reject_reason;				// 거부 사유
	private LocalDate suggested_at;				// 제안일시
	private LocalDate processed_at;				// 처리일시
}
