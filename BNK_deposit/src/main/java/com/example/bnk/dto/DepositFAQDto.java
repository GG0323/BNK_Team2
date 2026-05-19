package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor					// 예금 FAQ
public class DepositFAQDto {
	private long faq_no;			// FAQ PK
	private long employee_no;		// 등록 직원 FK
	private String question;		// 질문
	private String answer;			// 답변
	private int display_order;		// 노출 순서
	private String use_yn;			// 사용 여부
	private LocalDate created_at;	// 등록일
	private LocalDate updated_at;	// 수정일
}
