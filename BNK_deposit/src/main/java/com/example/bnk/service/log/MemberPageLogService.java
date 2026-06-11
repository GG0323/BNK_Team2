package com.example.bnk.service.log;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.log.IMemberPageLogDao;
import com.example.bnk.dto.log.MemberPageLogDto;

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
                        + " (url=" + dto.getRequestUrl() + ")");
            }
        } catch (Exception e) {
            System.out.println("[페이지로그] 저장 실패 (url=" + dto.getRequestUrl() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /** 전체 로그 조회 (관리용) */
    public List<MemberPageLogDto> allLog() {
    	System.out.println("전체 page log 조회");
        return logDao.allLog();
    }
    
    
    
}
