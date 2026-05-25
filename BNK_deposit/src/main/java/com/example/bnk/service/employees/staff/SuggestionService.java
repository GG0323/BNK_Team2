package com.example.bnk.service.employees.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.product.IProductSuggestionDao;
import com.example.bnk.dto.product.ProductSuggestionDto;

@Service
public class SuggestionService {
	
	@Autowired
	private IProductSuggestionDao suggestionDao;
	
	public int writeSuggestion(ProductSuggestionDto suggestionDto) {
		
		int result = suggestionDao.writeSuggestion(suggestionDto);
		
		return result;
	}
	
}
