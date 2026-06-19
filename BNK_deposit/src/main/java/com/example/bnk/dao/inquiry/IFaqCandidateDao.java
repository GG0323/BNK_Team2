package com.example.bnk.dao.inquiry;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.inquiry.FaqCandidateDto;

@Mapper
public interface IFaqCandidateDao {

	// 후보 조회
	List<FaqCandidateDto> selectPendingCandidates();
	
	// 스테이터스 수정
	int updateStatus(@Param("candidateNo") Long candidateNo, @Param("status") String status);
	
	// faq신규 등록
	int approveCandidate(@Param("candidateNo") Long candidateNo,
			 			 @Param("status") String status,
			 			 @Param("answer") String answer);
	
	
	
}
