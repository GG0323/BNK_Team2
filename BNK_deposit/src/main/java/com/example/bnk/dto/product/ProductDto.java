package com.example.bnk.dto.product;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDto {
	// 상품 Dto
	
	private long product_no;                // pk 
	private String product_name;            // 상품명
	private String product_type;            // 상품 구분 DEPOSIT:예금, SAVINGS:적금
	private double min_interest_rate;       // 기본/최저 금리
	private double max_interest_rate;       // 최고 우대 금리
	private String interest_payment_type;   // 이자 지급 방식 MATURITY:만기일시 MONTHLY:월이자 SETTLEMENT:결산주기 
	private String interest_calc_type;      // 이자 계산 방식 SIMPLE:단리 COMPOUND:복리
	private LocalDate sale_start_date;      // 판매 시작일
	private LocalDate sale_end_date;        // 판매 종료일
	private String product_status;          // 판매상태 DEFAULT SALE:판매중,  NON_SALE:판매중지
	private String branch_join_yn;          // 지점 가입 가능 여부  DEFAULT 'N' ('Y','N')
	private String internet_join_yn;        // 인터넷 가입 가능 여부 DEFAULT 'N' ('Y','N')
	private String mobile_join_yn;          // 모바일 가입 가능 여부 DEFAULT 'Y' ('Y','N')
	private LocalDate created_at;           // 생성일 DEFAULT SYSDATE
	private LocalDate updated_at;           // 수정일
	
	
}
