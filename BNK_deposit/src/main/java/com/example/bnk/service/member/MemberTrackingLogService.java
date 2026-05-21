package com.example.bnk.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.bnk.dao.member.IMemberTrackingLogDao;
import com.example.bnk.dto.member.MemberTrackingLogDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberTrackingLogService {
	
	private final IMemberTrackingLogDao trackingLogDao;

    public List<MemberTrackingLogDto> getRecentLogs(long memberNo) {
        return trackingLogDao.findRecentLogs(memberNo);
    }
    
    public int getLogCount(long memberNo) {
        return trackingLogDao.countLogsByMemberNo(memberNo);
    }
}
