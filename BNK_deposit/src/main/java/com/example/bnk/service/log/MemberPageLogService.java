package com.example.bnk.service.log;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.log.IMemberPageLogDao;
import com.example.bnk.dto.log.LogDailyStatDto;
import com.example.bnk.dto.log.LogPageStatSummaryDto;
import com.example.bnk.dto.log.LogSummaryDto;
import com.example.bnk.dto.log.LogTransitionStatDto;
import com.example.bnk.dto.log.MemberPageLogDto;
import com.example.bnk.dto.log.MemberPageLogJourneyDto;
import com.example.bnk.dto.log.MemberPageLogSearchDto;
import com.example.bnk.dto.log.PersonaProductLogStatDto;
import com.example.bnk.dto.log.SessionSummaryDto;

@Service
public class MemberPageLogService {
	
	@Autowired
	private IMemberPageLogDao logDao;

    
	
    /** 페이지 접근 로그 저장 — 비동기@Async 실행 */
    @Async("pageLogExecutor")
    public void log(MemberPageLogDto dto) {
        try {
        	// dao DB저장 실행
        	System.out.println("로그 저장");
            int result = logDao.insertLog(dto);
            if (result != 1) {
                System.out.println("[페이지로그] INSERT 결과가 1이 아님: " + result
                        + " (url=" + dto.getRequest_url() + ")");
            }
        } catch (Exception e) {
            System.out.println("[페이지로그] 저장 실패 (url=" + dto.getRequest_url() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /** 전체 로그 조회 (관리용) */
    public List<MemberPageLogDto> allLog() {
    	System.out.println("전체 page log 조회");
        return logDao.allLog();
    }
    
    // 조건에 맞는 로그 개수 검색
    public int countLogs(MemberPageLogSearchDto searchDto) {
    	int counts = logDao.countLogs(searchDto);
    	System.out.println("로그 개수 : "+ counts);
		return counts;
	}
	// 조건에 맞는 로그 내용 가져오기
	public List<MemberPageLogDto> searchLogs(MemberPageLogSearchDto searchDto) {
		List<MemberPageLogDto> list = logDao.searchLogs(searchDto);
		System.out.println("로그 내용 가져오기");
		return list;
	}
	// 세션 id로 가져오기
	public List<MemberPageLogJourneyDto> findJourney(String sessionId) {
		List<MemberPageLogJourneyDto> list = logDao.findBySessionId(sessionId);
		System.out.println("Session id로 사용자 여정 탐색");
		return list;
	}
	
	
	// 로그 전체 통계
	public LogSummaryDto statSummary(String fromDate, String toDate) {
		System.out.println("전체 로그 요약");
	    return logDao.logSummary(fromDate, toDate);
	}
	// 페이지 방문
	public List<LogPageStatSummaryDto> statByPage(String fromDate, String toDate) {
		System.out.println("페이지 총 방문");
	    return logDao.logPageStatSummary(fromDate, toDate);
	}
	// 일일 방문
	public List<LogDailyStatDto> statByDate(String fromDate, String toDate) {
		System.out.println("페이지 일일 방문");
	    return logDao.logDailyStat(fromDate, toDate);
	}
	// 페이지 이동
	public List<LogTransitionStatDto> statTransitions(String fromDate, String toDate) {
		System.out.println("페이지 이동 횟 수");
	    return logDao.logTransitionStat(fromDate, toDate);
	}
	// 페르소나별 인기 상품 조회
	public List<PersonaProductLogStatDto> statPersonaProduct(String fromDate, String toDate) {
		System.out.println("페르소나별 인기 상품");
		return logDao.statPersonaProduct(fromDate, toDate);
	}
	
	// 세션 개수
	public int countSessions(MemberPageLogSearchDto dto) {
		System.out.println("세션 개수");
		return logDao.countSessions(dto);
	}
	// 세션 목록
	public List<SessionSummaryDto> sessionList(MemberPageLogSearchDto dto) {
		System.out.println("세션 목록");
		return logDao.sessionList(dto);
	}
	
    
    
    
}
