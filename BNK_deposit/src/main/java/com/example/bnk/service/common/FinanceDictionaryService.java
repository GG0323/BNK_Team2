package com.example.bnk.service.common;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.common.IFinanceDictionaryDao;
import com.example.bnk.dto.common.FinanceDictionaryDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FinanceDictionaryService {

	private final IFinanceDictionaryDao financeDictionaryDao;
	
	// 금융용어사전 리스트 출력
	public List<FinanceDictionaryDto> getAllDictionarys(){
		return financeDictionaryDao.selectAllDictionarys();
	}
	
	// 금융용어 상세보기 페이지 (✨ 로직 업그레이드!)
	public FinanceDictionaryDto getDictionary(long dictionary_no) {
	    // 해당 용어의 조회수를 DB에서 먼저 1 증가. (UPDATE)
	    financeDictionaryDao.updateViewCount(dictionary_no);
	    
	    // 조회수가 올라간 최신 상태의 데이터를 가져와서 컨트롤러로 반환. (SELECT)
	    return financeDictionaryDao.selectDictionaryByNo(dictionary_no);
	}
	
	// 검색 로직 추가
	public List<FinanceDictionaryDto> searchDictionary(String keyword) {
		return financeDictionaryDao.searchDictionary(keyword);
	}
	
	// ✨ 수정 화면용 데이터 불러오기 (조회수 증가 없음!)
    public FinanceDictionaryDto getDictionaryForEdit(long dictionary_no) {
        return financeDictionaryDao.selectDictionaryByNo(dictionary_no);
    }

	// ✨ 용어 등록
	public void addDictionary(FinanceDictionaryDto dto) {
		financeDictionaryDao.insertDictionary(dto);
	}
	
	// ✨ 용어 수정
	public void modifyDictionary(FinanceDictionaryDto dto) {
		financeDictionaryDao.updateDictionary(dto);
	}
	
	// ✨ 용어 삭제
	public void removeDictionary(long dictionary_no) {
		financeDictionaryDao.deleteDictionary(dictionary_no);
	}
	
	// 검색 분류
	public List<FinanceDictionaryDto> searchDictionaryByType(String searchType, String keyword) {

	    if (keyword == null || keyword.trim().isEmpty()) {
	        return getAllDictionarys();
	    }

	    if (searchType == null || searchType.trim().isEmpty()) {
	        searchType = "all";
	    }

	    return financeDictionaryDao.searchDictionaryByType(searchType, keyword.trim());
	}
}
