package com.example.bnk.dto.member;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberProductDto {
    // TB_PRODUCT_SALES (가입 내역 테이블 정보)
    private long subscription_no;         // 가입 번호 (PK)
    private long member_no;               // 회원 번호 (FK)
    private long product_no;              // 상품 번호 (FK)
    private long account_no;              // 가입 후 생성된 계좌 번호
    private long subscription_amount;     // 가입 금액
    private double applied_interest_rate; // 실제 적용 금리
    private LocalDate subscribed_at;      // 가입일시
    private LocalDate maturity_date;      // 만기일
    private String subscription_status;   // 가입상태 (DRAFT, COMPLETE, EXPIRED)

    // TB_PRODUCT (상품 원장 테이블 정보 - JOIN으로 가져올 데이터)
    private String product_name;          // 상품명 (예: 모바일 전용 적금)
    private String product_type;          // 상품 구분 (DEPOSIT: 예금, SAVINGS: 적금)
}