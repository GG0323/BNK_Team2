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
public class AccountDto {
	private long account_no;
	private long member_no;
	private long account_number;
	private String account_alias;
	private long balance;
	private String account_status;
	private String account_purpose;
	private LocalDate opened_at;
	private LocalDate closed_at;
	private LocalDate updated_at;
}
