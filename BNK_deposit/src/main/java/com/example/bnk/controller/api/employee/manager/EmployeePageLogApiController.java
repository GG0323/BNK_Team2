package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.log.LogStatResponseDto;
import com.example.bnk.dto.log.MemberPageLogDto;
import com.example.bnk.dto.log.MemberPageLogJourneyDto;
import com.example.bnk.dto.log.MemberPageLogListResponseDto;
import com.example.bnk.dto.log.MemberPageLogSearchDto;
import com.example.bnk.service.log.MemberPageLogService;

@RestController
@RequestMapping("/api/employee/pageLog")
public class EmployeePageLogApiController {
	
	@Autowired
    private MemberPageLogService logService;
	
	
    //로그 검색 조건에 맞는 로그 리스트를 페이지 규모에 맞게 가져온다.
    @GetMapping("/list")
    public MemberPageLogListResponseDto logList(
    		MemberPageLogSearchDto searchDto // 검색 조건 Dto
    		) {
    	System.out.println("검색조건 확인" + searchDto.toString());
    	
        int totalCount = logService.countLogs(searchDto); // 조건에 맞는 로그 개수 검색
        int totalPages = (int) Math.ceil((double) totalCount / searchDto.getSize());
        if (totalPages == 0) {
        	totalPages = 1;
        }
        
        // 페이지 범위 보정 (검색 조건 변경 후 페이지가 범위를 벗어나는 경우)
        if (searchDto.getPage() > totalPages) searchDto.setPage(totalPages);
        if (searchDto.getPage() < 1) searchDto.setPage(1);
        
        List<MemberPageLogDto> logs = logService.searchLogs(searchDto); // 조건에 맞는 로그 내용 가져오기
        
        // 응답 전용 객체를 생성해 봔환한다. 
        MemberPageLogListResponseDto resDto = new MemberPageLogListResponseDto(logs, totalCount, totalPages, searchDto.getPage());
        
        return resDto;
    }
    
    // 세션 id 별 여정 탐색
    @GetMapping("/session/{sessionId}")
    public List<MemberPageLogJourneyDto> sessionJourney(
    		@PathVariable("sessionId") String sessionId
    		) {
        return logService.findJourney(sessionId);
    }
    
    // stats 페이지 정보 로딩
    @GetMapping("/stats")
    public LogStatResponseDto stats(
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate",   required = false) String toDate
            ) {
        return new LogStatResponseDto(
                logService.statSummary(fromDate, toDate),
                logService.statByPage(fromDate, toDate),
                logService.statByDate(fromDate, toDate),
                logService.statTransitions(fromDate, toDate)
        );
    }
	
    
    
	
}
