package com.example.bnk.dto.product;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductJoinStatusDto {
	private Long subscriptionNo;
	private long productNo;
	private String productName;
	private String productType;
	private long minJoinAmount;
	private long maxJoinAmount;
	private long depositUnit;
	private int minTermMonths;
	private int maxTermMonths;
	private String subscriptionStatus;
	private String currentStep;
	private boolean accountRequired;
	private Long accountNo;
	private Long linkedAccountId;
	private Long subscriptionAmount;
	private Long subscriptionMonths;
	private Double appliedInterestRate;
	private Integer requiredTermsAgreed;
	private Integer optionalTermsAgreed;
	private LocalDate maturityDate;
	private String message;
}
