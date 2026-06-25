package com.example.bnk.service.member;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IAccountDao;
import com.example.bnk.dto.member.AccountCreateDto;
import com.example.bnk.dto.member.AccountDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {
	
	private static final Set<String> ACCOUNT_PURPOSE_CODES = Set.of(
			"SALARY",
			"PART_TIME_SALARY",
			"PENSION",
			"BUSINESS",
			"GROUP",
			"UTILITY_PAYMENT",
			"LIVING_EXPENSE",
			"ETC"
	);

	private final IAccountDao accountDao;

	// 계좌 목록 조회
    public List<AccountDto> getAccounts(String username) {
        return accountDao.findAccountsByMemberNo(username);
    }

    // 계좌 상세 정보 조회
    public AccountDto getAccountDetail(long accountNo) {
        return accountDao.findAccountByAccountNo(accountNo);
    }

    public AccountDto openDemandDepositAccount(long memberNo, String accountPurpose) {
    	return openDemandDepositAccount(memberNo, accountPurpose, null);
    }

    public AccountDto openDemandDepositAccount(long memberNo, String accountPurpose, String encryptedAccountPassword) {
    	String normalizedPurpose = normalizeAccountPurpose(accountPurpose);

    	AccountCreateDto account = new AccountCreateDto();
    	account.setMemberNo(memberNo);
    	account.setAccountAlias("입출금 계좌");
    	account.setAccountPurpose(normalizedPurpose);
    	account.setAccountPassword(encryptedAccountPassword);

    	accountDao.insertDemandDepositAccount(account);

    	if (account.getAccountNo() == null) {
    		throw new IllegalStateException("Failed to create account.");
    	}

    	return accountDao.findAccountByAccountNo(account.getAccountNo());
    }

    public int cancelAccountOpening(long memberNo) {
    	return accountDao.deleteAuthenticationByMemberNo(memberNo);
    }

    private String normalizeAccountPurpose(String accountPurpose) {
    	String normalized = accountPurpose == null || accountPurpose.isBlank()
    			? "ETC"
    			: accountPurpose.trim();

    	if (!ACCOUNT_PURPOSE_CODES.contains(normalized)) {
    		throw new IllegalArgumentException("Invalid account purpose.");
    	}

    	return normalized;
    }
}
