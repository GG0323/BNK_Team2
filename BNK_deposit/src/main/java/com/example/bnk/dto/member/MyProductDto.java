package com.example.bnk.dto.member;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class MyProductDto {

    private long subscription_no;
    private long member_no;
    private long product_no;
    private long account_no;
    private String product_name;
    private String product_type;

    private BigDecimal min_interest_rate;
    private BigDecimal max_interest_rate;

    private String interest_payment_type;
    private String interest_calc_type;

    private String join_channel;
    private BigDecimal applied_interest_rate;

    private int subscription_months;
    private BigDecimal subscription_amount;
    private BigDecimal auto_transfer_amount;

    private Long linked_account_id;

    private LocalDate subscribed_at;
    private LocalDate maturity_date;

    private String subscription_status;

    // 만기일까지 남은 일수
    private int remaining_days;

    // 세전 단순 예상 이자
    private BigDecimal expected_interest;

    // 세전 단순 예상 만기 수령액
    private BigDecimal expected_total_amount;
}