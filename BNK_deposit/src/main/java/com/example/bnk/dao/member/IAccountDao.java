package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.AccountCreateDto;
import com.example.bnk.dto.member.AccountDto;

@Mapper
public interface IAccountDao {

	 // 회원 번호로 보유 계좌 목록 조회
    List<AccountDto> findAccountsByMemberNo(@Param("username") String username);

    // 특정 계좌 번호로 계좌 상세 정보 1건 조회
    AccountDto findAccountByAccountNo(@Param("accountNo") long accountNo);

    // account_no로 회원의 주계좌 정보 받기
    AccountDto findUsersAccount(@Param("account_no") long account_no);
    
    // member_no로 주계좌 찾기
    long findUsingAccountNo(@Param("member_no") long member_no);

    Long findActiveAccountNoByMemberNo(@Param("member_no") long member_no);

    int insertDemandDepositAccount(AccountCreateDto dto);

    int insertProductAccount(AccountCreateDto dto);

    int deleteAuthenticationByMemberNo(@Param("member_no") long member_no);
    
    // 환금해주기
    int interestToMyAccount(@Param("balance") long balance, @Param("applied_interest_rate") double applied_interest_rate, @Param("member_no") long member_no);
    
    // 가입 상태 해지하기로 바꾸기
    int changeSubscriptionStatus(@Param("account_no") long account_no);
    
    // 환금 끝나고 계좌 비활성 만들기.
    int changeAccountStatus(@Param("account_no") long account_no);
}
