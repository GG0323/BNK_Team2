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
		private Long productNo;
		private Long product_no;
		private Long subscriptionNo;
		private Long subscription_no;
		private boolean requiredTermsAgreed;
		private boolean optionalTermsAgreed;

		public Long productNo() {
			return productNo != null ? productNo : product_no;
		}

		public Long subscriptionNo() {
			return subscriptionNo != null ? subscriptionNo : subscription_no;
		}
	}

	@Data
	public static class ContractRequest {
		private Long subscriptionNo;
		private Long subscription_no;
		private Long linkedAccountNo;
		private Long linked_account_no;
		private Long linkedAccountId;
		private Long linked_account_id;
		private Long subscriptionAmount;
		private Long subscription_amount;
		private Long subscriptionMonths;
		private Long subscription_months;

		public Long subscriptionNo() {
			return subscriptionNo != null ? subscriptionNo : subscription_no;
		}

		public Long linkedAccountNo() {
			if (linkedAccountNo != null) return linkedAccountNo;
			if (linked_account_no != null) return linked_account_no;
			if (linkedAccountId != null) return linkedAccountId;
			return linked_account_id;
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
		private String accountPassword;
		private String account_password;
		private Integer frontIndex;
		private Integer front_index;
		private Integer backIndex;
		private Integer back_index;
		private String frontAnswer;
		private String front_answer;
		private String backAnswer;
		private String back_answer;

		public Long subscriptionNo() {
			return subscriptionNo != null ? subscriptionNo : subscription_no;
		}

		public String accountPassword() {
			return accountPassword != null ? accountPassword : account_password;
		}

		public Integer frontIndex() {
			return frontIndex != null ? frontIndex : front_index;
		}

		public Integer backIndex() {
			return backIndex != null ? backIndex : back_index;
		}

		public String frontAnswer() {
			return frontAnswer != null ? frontAnswer : front_answer;
		}

		public String backAnswer() {
			return backAnswer != null ? backAnswer : back_answer;
		}
	}
}
