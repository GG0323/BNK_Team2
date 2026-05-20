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

    // 상품 목록 화면용 DTO

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
}