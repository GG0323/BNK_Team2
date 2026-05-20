package com.example.bnk.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductCompareViewDto {

    // 상품 비교 화면용 DTO

    private long product_no;                 // 상품 번호
    private String product_name;             // 상품명
    private String product_type;             // 상품 구분 DEPOSIT / SAVINGS

    private double min_interest_rate;        // 최저 금리
    private double max_interest_rate;        // 최고 금리

    private String interest_payment_type;    // 이자 지급 방식
    private String interest_calc_type;       // 이자 계산 방식

    private String branch_join_yn;           // 영업점 가입 가능 여부
    private String internet_join_yn;         // 인터넷 가입 가능 여부
    private String mobile_join_yn;           // 모바일 가입 가능 여부

    // TB_PRODUCT_DESCRIPTION
    private String join_method_desc;         // 가입방법 설명

    // TB_PRODUCT_CONDITION
    private long min_join_amount;            // 최소 가입금액
    private long max_join_amount;            // 최대 가입금액
    private long deposit_unit;               // 입금 단위
    private int min_term_months;             // 최소 가입기간
    private int max_term_months;             // 최대 가입기간
    private String fixed_term_yn;            // 고정기간 여부
    private String fixed_term_values;        // 고정 가입기간 값
    private String condition_note;           // 가입조건 / 우대조건 비고
    private String depositor_protection_yn;  // 예금자보호 여부

    // TB_PRODUCT_RATE - 만기 금리
    private String maturity_rate_label;      // 만기 금리 조건명
    private String maturity_annual_rate;     // 만기 연이율
    private String maturity_return_rate;     // 만기 연수익률

    // TB_PRODUCT_RATE - 만기 후 금리
    private String after_maturity_rate_label;    // 만기 후 금리 조건명
    private String after_maturity_annual_rate;   // 만기 후 연이율

    // TB_PRODUCT_RATE - 중도해지 금리
    private String early_rate_label;         // 중도해지 금리 조건명
    private String early_annual_rate;        // 중도해지 연이율
}