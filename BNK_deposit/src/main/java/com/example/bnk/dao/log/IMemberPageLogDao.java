package com.example.bnk.dao.log;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.log.MemberPageLogDto;

@Mapper
public interface IMemberPageLogDao {
	
    // 로그 삽입
    int insertLog(@Param("dto") MemberPageLogDto dto);
    // 로그 조회
    List<MemberPageLogDto> allLog();
    
    
}
