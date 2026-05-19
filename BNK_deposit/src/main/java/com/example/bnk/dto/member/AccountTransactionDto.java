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
public class AccountTransactionDto {
	// 거래 내역 (LOG)
	
	private long transaction_no;         // pk
	private long from_account_number;    // 출금 계좌
	private long to_account_number;      // 입금 계좌
	private long amount;                 // 거래 금액
	private String transaction_type;     // 거래 유형 DEPOSIT:입금 WITHDRAW:출금 TRANSFER:이체
	private long balance_after;			 // 거래 후 잔액
	private String memo;				 // 메모
	private String counterparty_name;	 // 적요/거래처명
	private LocalDate transaction_at;	 // 거래일시 DEFAULT SYSDATE
}
