package com.example.bnk.service.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.product.IApporvedSuggestionDao;
import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;

@Service
@Transactional
public class ApprovedSuggestionService {
	
	@Autowired
	private IApporvedSuggestionDao approvedDao;
	
	
	public List<ApprovedSuggestionDetailDto> approvedList(long employee_no) {
		
		List<ApprovedSuggestionDetailDto> list = approvedDao.approvedList(employee_no);
		
		return list;
	}


	public ApprovedSuggestionDetailDto approvedDetail(long suggestion_no) {
		
		ApprovedSuggestionDetailDto detail = approvedDao.approvedDetail(suggestion_no);
		
		return detail;
	}

}
