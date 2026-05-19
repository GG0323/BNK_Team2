package com.example.bnk.dto;

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
	// 계좌 Dto
	
	private long account_no;        // 계좌 pk
	private long member_no;         // 멤버 fk
	private long account_number;    // 실제 계좌번호
	private String account_alias;   // 계좌 별명
	private long balance;           // 계좌 잔액 디폴트 0 
	private String account_status;  // 계좌 상태 디폴트 ACTIVE, (INACTIVE, DORMANT, CLOSED)
	private LocalDate opened_at;    // 계좌 개설일시 DEFAULT SYSDATE
	private LocalDate closed_at;    // 계좌 해지일시
	private LocalDate updated_at;   // 수정일
	
}
