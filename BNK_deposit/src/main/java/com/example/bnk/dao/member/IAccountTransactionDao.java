package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.member.AccountTransactionDto;

@Mapper
public interface IAccountTransactionDao {

	// 특정 계좌 번호로 '거래 내역 리스트' 여러 건 조회
    List<AccountTransactionDto> findTransactionsByAccountNo(long accountNo);
}
