package com.example.bnk.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductJoinEntryStatusDto {
	private long memberNo;
	private String memberStatus;
	private boolean regularMember;
	private boolean hasActiveAccount;
	private boolean accountRequired;
	private Long activeAccountNo;
	private long productNo;
	private String productName;
	private String productType;
	private boolean joinableProduct;
	private boolean canEnterJoin;
	private String message;
}
