package com.example.bnk.dto.member;

import lombok.Data;

@Data
public class AccountCreateDto {
	private Long accountNo;
	private long memberNo;
	private String accountAlias;
	private String accountPassword;
	private String accountPurpose;
}
