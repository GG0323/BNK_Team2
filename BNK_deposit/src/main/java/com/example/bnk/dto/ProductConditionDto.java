package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductConditionDto {
	// 상품 가입 조건
	
	private long condition_no;              // 가입조건 PK
	private long product_no;                // 상품 FK
	private long min_age;                   // 최소 가입 나이
	private long max_age;                   // 최대 가입 나이
	private String foreigner_available_yn;  // 외국인 가입 가능 여부
	private String overseas_tax_yn;         // 해외 납세 의무 확인 필요 여부 DEFAULT 'Y'   Y: 확인 필요 / N: 확인 불필요
	private String gender;                  // 성별 제한  M: 남성 / F: 여성 / NULL: 제한 없음
	private String customer_type;           // 고객 유형 DEFAULT 'ALL'   PERSONAL:개인 BUSINESS:사업자 ALL:제한 없음
	private String tax_benefit_yn;          // 세제혜택 여부 DEFAULT 'N'          Y: 세제혜택 가능 / N: 불가
	private String non_taxable_savings_yn;  // 비과세종합저축 가능 여부 DEFAULT 'N'  Y: 가능 / N: 불가
	private String depositor_protection_yn; // 예금자보호 대상 여부 DEFAULT 'Y'     Y: 대상 / N: 비대상
	private long min_join_amount;           // 최소 가입금액
	private long max_join_amount;           // 최대 가입금액
	private long deposit_unit;              // 입금 단위
	private long min_term_months;           // 최소 가입기간
	private long max_term_months;           // 최대 가입기간
	private String term_unit_type;          // 가입기간 단위 유형 DEFAULT 'MONTH'  DAY:일 MONTH:월 YEAR:년
	private long term_unit_value;           // 가입기간 단위 값  DEFAULT 1 예) 1개월 단위
	private String fixed_term_yn;           // 가입기간 고정 여부 DEFAULT 'N' Y: 지정 기간만 가능 N: 기간 지정 없읔
	private String fixed_term_values;       // 고정 가입기간 값
	private String condition_note;          // 가입조건 비고
	private LocalDate created_at;           // 생성일 DEFAULT SYSDATE
	private LocalDate updated_at;           // 수정일
}
