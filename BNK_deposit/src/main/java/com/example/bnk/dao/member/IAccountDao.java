package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.member.AccountDto;

@Mapper
public interface IAccountDao {

	List<AccountDto> findAccountsByMemberNo(long memberNo);

}
