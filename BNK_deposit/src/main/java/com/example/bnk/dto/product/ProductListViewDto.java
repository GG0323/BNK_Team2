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
public class ProductListViewDto {

    private long product_no;                 // 상품 번호
    private String product_name;             // 상품명
    private String product_type;             // 상품 구분 DEPOSIT / SAVINGS

    private double min_interest_rate;        // 최저 금리
    private double max_interest_rate;        // 최고 금리

    private String interest_payment_type;    // 이자 지급 방식
    private String interest_calc_type;       // 이자 계산 방식

    private String product_status;           // 판매 상태
    private String branch_join_yn;           // 영업점 가입 가능 여부
    private String internet_join_yn;         // 인터넷 가입 가능 여부
    private String mobile_join_yn;           // 모바일 가입 가능 여부

    private LocalDate sale_start_date;       // 판매 시작일
    private LocalDate sale_end_date;         // 판매 종료일

    // TB_PRODUCT_DESCRIPTION
    private String subtitle;                 // 상품 부제목
    private String content;                  // 상품 설명 문구
    private String image_url;                // 상품 이미지

    // TB_PRODUCT_CONDITION
    private String customer_type;            // 가입 대상 유형 ALL / PERSONAL / BUSINESS
    private Integer min_age;                 // 최소 가입 나이
    private Integer max_age;                 // 최대 가입 나이
    private String gender;                   // 가입 가능 성별 M / F / NULL
    private String tax_benefit_yn;           // 세제혜택 여부
    private String depositor_protection_yn;  // 예금자보호 여부
    private Long min_join_amount;            // 최소 가입 금액
    private Long max_join_amount;            // 최대 가입 금액
    private Integer min_term_months;         // 최소 가입 기간 개월
    private Integer max_term_months;         // 최대 가입 기간 개월

    // 화면 표시용
    private String joined_yn;                // 로그인 회원의 가입 여부 Y / N
}