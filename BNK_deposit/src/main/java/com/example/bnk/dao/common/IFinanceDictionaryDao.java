package com.example.bnk.dao.common;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.common.FinanceDictionaryDto;

@Mapper
public interface IFinanceDictionaryDao {

	// 금융용어사전 리스트 출력
	List<FinanceDictionaryDto> selectAllDictionarys();
	
	// 금융용어 상세보기 페이지
	FinanceDictionaryDto selectDictionaryByNo(long dictionary_no);
	
	// ✨ 특정 용어의 조회수 1 증가시키기
	void updateViewCount(long dictionary_no);
	
	// 검색 로직
	List<FinanceDictionaryDto> searchDictionary(String keyword);
	
	// 챗봇용 금융용어 검색
	List<FinanceDictionaryDto> searchDictionaryForChat(
	        @Param("question") String question,
	        @Param("keyword") String keyword
	);
	
	// 카테고리 기준 용어 조회
	List<FinanceDictionaryDto> selectDictionaryByCategory(
	        @Param("category") String category
	);
	
	// ✨ 용어 등록 (Create)
	int insertDictionary(FinanceDictionaryDto dto);
		
	// ✨ 용어 수정 (Update)
	int updateDictionary(FinanceDictionaryDto dto);
	
	// ✨ 용어 삭제 (Delete)
	int deleteDictionary(long dictionary_no);
	
	// 검색 주제
	List<FinanceDictionaryDto> searchDictionaryByType(
	        @Param("searchType") String searchType,
	        @Param("keyword") String keyword
	);
	
}
