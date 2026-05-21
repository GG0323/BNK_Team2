package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.AccountTransactionDto;

@Mapper
public interface IAccountDao {

	// 회원 번호로 보유 계좌 목록 조회
    List<AccountDto> findAccountsByMemberNo(long memberNo);

    // 1. 특정 계좌 번호로 '계좌 상세 정보' 1건 조회
    AccountDto findAccountByAccountNo(long accountNo);

    // 2. 특정 계좌 번호로 '거래 내역 리스트' 여러 건 조회
    List<AccountTransactionDto> findTransactionsByAccountNo(long accountNo);

}
