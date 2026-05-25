package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductSuggestionDto;
import com.example.bnk.dto.product.suggestion.SuggestionListDto;


@Mapper
public interface IProductSuggestionDao {
	
	public List<ProductSuggestionDto> showPrdSugt();
	
	//제안서 작성
	public int writeSuggestion(@Param("dto")ProductSuggestionDto suggestionDto);
	
	
	// 나에게 온 제안서
	public List<SuggestionListDto> mySuggestionList(@Param("manager_employee_no") long employee_no);
	
	// 제안서 상세보기
	public SuggestionListDto suggestionReview(@Param("suggestion_no") long suggestion_no);

	// 제안서 승인
	public int approveSuggestion(@Param("suggestion_no") long suggestion_no);
	// 승인한 제안서 테이블 인서트
	public void insertApprovedProduct(@Param("suggestion_no") long suggestion_no);
	
	// 제안서 거부
	public int rejectSuggestion(@Param("suggestion_no") long suggestion_no, 
	                     		@Param("reject_reason") String reject_reason);
	

	
	
	
}
