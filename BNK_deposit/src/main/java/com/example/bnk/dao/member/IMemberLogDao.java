package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.BankMemberLogDto;

@Mapper
public interface IMemberLogDao {

	int insertLog(@Param("dto") BankMemberLogDto insertDto);

	List<BankMemberLogDto> allLog();

	long findByUserID(@Param("userid") String userid);

}
