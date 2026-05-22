package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.AccountDto;

@Mapper
public interface IAccountDao {

	 // 회원 번호로 보유 계좌 목록 조회
    List<AccountDto> findAccountsByMemberNo(@Param("memberNo") long memberNo);

    // 특정 계좌 번호로 계좌 상세 정보 1건 조회
    AccountDto findAccountByAccountNo(@Param("accountNo") long accountNo);

}
