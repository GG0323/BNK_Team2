package com.example.bnk.dto.product;

import lombok.Data;

public class ProductJoinRequests {
	private ProductJoinRequests() {}

	@Data
	public static class StartRequest {
		private Long productNo;
		private Long product_no;

		public Long productNo() {
			return productNo != null ? productNo : product_no;
		}
	}

	@Data
	public static class TermsRequest {
		private Long subscriptionNo;
		private Long subscription_no;
		private Long subscriptionAmount;
		private Long subscription_amount;
		private Long subscriptionMonths;
		private Long subscription_months;
		private boolean requiredTermsAgreed;
		private boolean optionalTermsAgreed;

		public Long subscriptionNo() {
			return subscriptionNo != null ? subscriptionNo : subscription_no;
		}

		public Long subscriptionAmount() {
			return subscriptionAmount != null ? subscriptionAmount : subscription_amount;
		}

		public Long subscriptionMonths() {
			return subscriptionMonths != null ? subscriptionMonths : subscription_months;
		}
	}

	@Data
	public static class CompleteRequest {
		private Long subscriptionNo;
		private Long subscription_no;
		private String accountPurpose;
		private String account_purpose;

		public Long subscriptionNo() {
			return subscriptionNo != null ? subscriptionNo : subscription_no;
		}

		public String accountPurpose() {
			return accountPurpose != null ? accountPurpose : account_purpose;
		}
	}
}
