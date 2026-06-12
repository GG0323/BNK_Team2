package com.example.bnk.dao.log;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.log.LogDailyStatDto;
import com.example.bnk.dto.log.LogPageStatSummaryDto;
import com.example.bnk.dto.log.LogSummaryDto;
import com.example.bnk.dto.log.LogTransitionStatDto;
import com.example.bnk.dto.log.MemberPageLogDto;
import com.example.bnk.dto.log.MemberPageLogJourneyDto;
import com.example.bnk.dto.log.MemberPageLogSearchDto;

@Mapper
public interface IMemberPageLogDao {
	
    // 로그 삽입
    int insertLog(@Param("dto") MemberPageLogDto dto);
    // 로그 조회
    List<MemberPageLogDto> allLog();
    
    // 조건에 맞는 로그 개수 검색
    int countLogs(@Param("dto") MemberPageLogSearchDto searchDto);
    // 조건에 맞는 로그 내용 가져오기
    List<MemberPageLogDto> searchLogs(@Param("dto") MemberPageLogSearchDto searchDto);
	
    
    // 세션 별 여정 검색
    List<MemberPageLogJourneyDto> findBySessionId(String sessionId);
    
    
    // 로그 전체
    LogSummaryDto logSummary(@Param("from_date") String fromDate, @Param("to_date") String toDate);
    // 페이지
    List<LogPageStatSummaryDto> logPageStatSummary(@Param("from_date") String fromDate, @Param("to_date") String toDate);
    // 일일 방문
    List<LogDailyStatDto> logDailyStat(@Param("from_date") String fromDate, @Param("to_date") String toDate);
    // 페이지 이동 수
    List<LogTransitionStatDto> logTransitionStat(@Param("from_date") String fromDate, @Param("to_date") String toDate);
    
    
    
    
    
}
