package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.member.MemberTrackingLogDto;

@Mapper
public interface IMemberTrackingLogDao {

	List<MemberTrackingLogDto> findRecentLogs(long memberNo);

	int countLogsByMemberNo(long memberNo);

}
