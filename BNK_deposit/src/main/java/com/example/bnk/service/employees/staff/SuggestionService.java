package com.example.bnk.service.employees.staff;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.product.IProductSuggestionDao;
import com.example.bnk.dto.product.ProductSuggestionDto;
import com.example.bnk.dto.product.suggestion.SuggestionListDto;

@Service
@Transactional
public class SuggestionService {
	
	@Autowired
	private IProductSuggestionDao suggestionDao;
	
	// 제안서 작성
	public int writeSuggestion(ProductSuggestionDto suggestionDto) {
		
		int result = suggestionDao.writeSuggestion(suggestionDto);
		
		return result;
	}
	
	// 나에게 온 제안서
	public List<SuggestionListDto> mySuggestionList(long employee_no) {
		
		List<SuggestionListDto> sugList = suggestionDao.mySuggestionList(employee_no);
		
		return sugList;
	}
	// 제안서 상세 페이지
	public SuggestionListDto suggestionReview(long suggestion_no) {
		
		SuggestionListDto reviewPage = suggestionDao.suggestionReview(suggestion_no);
		
		return reviewPage;
	}
	// 제안서 승인
	public int approveSuggestion(long suggestion_no) {
		// 제안서 승인
		int result = suggestionDao.approveSuggestion(suggestion_no);
		// 승인 테이블 인서트
		suggestionDao.insertApprovedProduct(suggestion_no);
		
		return result;
	}
	// 제안서 거부
	public int rejectSuggestion(long suggestion_no, String reject_reason) {
		int result = suggestionDao.rejectSuggestion(suggestion_no, reject_reason);
		return result;
	}
	
	
	
	
	
	
	
}
