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
public class ProductDetailViewDto {

    // 상품 상세 화면용 DTO

    private long product_no;                 // 상품 번호
    private String product_name;             // 상품명
    private String product_type;             // 상품 구분 DEPOSIT / SAVINGS

    private double min_interest_rate;        // 최저 금리
    private double max_interest_rate;        // 최고 금리

    private String interest_payment_type;    // 이자 지급 방식
    private String interest_calc_type;       // 이자 계산 방식

    private LocalDate sale_start_date;       // 판매 시작일
    private LocalDate sale_end_date;         // 판매 종료일

    private String product_status;           // 판매 상태
    private String branch_join_yn;           // 영업점 가입 가능 여부
    private String internet_join_yn;         // 인터넷 가입 가능 여부
    private String mobile_join_yn;           // 모바일 가입 가능 여부

    // TB_PRODUCT_DESCRIPTION
    private String subtitle;                 // 부제목
    private String content;                  // 홍보 문구
    private String eligibility_desc;         // 가입 자격
    private String period_desc;              // 가입 기간 설명
    private String amount_desc;              // 가입 금액 설명
    private String preferential_rate_summary;// 우대금리 요약
    private String product_feature_desc;     // 상품 특징
    private String deposit_subject_desc;     // 예금 과목
    private String payment_method_desc;      // 납입 방법
    private String join_method_desc;         // 가입 방법 설명
    private String required_document_desc;   // 필요 서류
    private String tax_benefit_desc;         // 세제 혜택
    private String interest_rate_desc;       // 적용 금리
    private String interest_payment_desc;    // 이자 지급 설명
    private String preferential_rate_desc;   // 우대 이율 설명
    private String expected_interest_desc;   // 예상 수취 이자 안내
    private String caution_note;             // 유의사항
    private String reference_note;           // 참고사항
    private String sale_period_desc;         // 판매 기간 문구
    private String non_taxable_savings_desc; // 비과세종합저축 설명
    private String disclosure_approval_desc; // 공시 승인 번호
    private String image_url;                // 이미지 URL

    // TB_PRODUCT_CONDITION
    private int min_age;                     // 최소 가입 나이
    private int max_age;                     // 최대 가입 나이
    private String customer_type;            // 고객 유형
    private String tax_benefit_yn;           // 세제 혜택 여부
    private String depositor_protection_yn;  // 예금자보호 여부
    private long min_join_amount;            // 최소 가입금액
    private long max_join_amount;            // 최대 가입금액
    private long deposit_unit;               // 입금 단위
    private int min_term_months;             // 최소 가입 기간
    private int max_term_months;             // 최대 가입 기간
    private String fixed_term_yn;            // 고정 기간 여부
    private String fixed_term_values;        // 고정 가입 기간 값
    private String condition_note;           // 가입 조건 비고
}