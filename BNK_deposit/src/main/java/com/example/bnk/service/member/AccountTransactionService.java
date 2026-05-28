package com.example.bnk.service.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IAccountTransactionDao;
import com.example.bnk.dto.member.AccountTransactionDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountTransactionService {

	private final IAccountTransactionDao accountTransactionDao;
	
	// 거래 내역 리스트 조회
    public List<AccountTransactionDto> getTransactions(long accountNo) {
        return accountTransactionDao.findTransactionsByAccountNo(accountNo);
    }
}
