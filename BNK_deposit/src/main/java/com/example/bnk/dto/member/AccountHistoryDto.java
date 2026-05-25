package com.example.bnk.dto.member;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 계좌 상세 + 거래내역(/api/accounts/{accountNo}/history) 화면을 위한 응답 묶음 DTO.
 * 계좌 상세 정보와 그 계좌의 거래내역 목록을 함께 내려준다.
 */
@Getter
@Builder
public class AccountHistoryDto {

	private final AccountDto account;                       // 계좌 상세 정보
	private final List<AccountTransactionDto> transactionList; // 거래내역 목록
}