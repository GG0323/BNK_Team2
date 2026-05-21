package com.example.bnk.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.bnk.dao.member.IAccountDao;
import com.example.bnk.dto.member.AccountDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	
	private final IAccountDao accountDao;

    public List<AccountDto> getAccounts(long memberNo) {
        return accountDao.findAccountsByMemberNo(memberNo);
    }
}
