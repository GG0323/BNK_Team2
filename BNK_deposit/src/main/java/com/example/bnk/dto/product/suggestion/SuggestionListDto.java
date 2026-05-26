package com.example.bnk.dto.product.suggestion;



import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor		
public class SuggestionListDto {
	// 나에게 온 제안서를 조회
	
	private long suggestion_no;					// 제안서 PK
	
	private long employee_no;					// 제안 직원 FK
	private String employee_name;       // 제안서 작성자 이름
	private String job_title;			// 작성자 직급
	private long   dept_no;        		// 작성자 부서
	
	
	private long manager_employee_no;			// 승인 책임자 FK
	private String manager_name;  			    // 승인 책임자 이름 (AS manager_name)
	
	private String product_name;				// 제안 상품명
	private String suggestion_content;			// 제안 내용
	private double proposed_min_interest_rate;	// 제안 최저금리
	private double proposed_max_interest_rate;	// 제안 최고금리
	private String approval_status;				// 승인 상태
	private String reject_reason;				// 거부 사유
	private LocalDate suggested_at;				// 제안일시
	private LocalDate processed_at;	            // 승인일시
}
