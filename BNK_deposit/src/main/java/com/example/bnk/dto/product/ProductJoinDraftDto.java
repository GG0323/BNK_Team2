package com.example.bnk.dto.product;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProductJoinDraftDto {
	private Long subscription_no;
	private long member_no;
	private long product_no;
	private Long account_no;
	private String join_channel;
	private Double applied_interest_rate;
	private Long subscription_months;
	private Long subscription_amount;
	private Long auto_transfer_amount;
	private Long linked_account_id;
	private LocalDate subscribed_at;
	private LocalDate maturity_date;
	private String subscription_status;
	private Integer required_terms_agreed;
	private Integer optional_terms_agreed;
	private LocalDate created_at;
	private LocalDate updated_at;
	private String product_name;
	private String product_type;
}
