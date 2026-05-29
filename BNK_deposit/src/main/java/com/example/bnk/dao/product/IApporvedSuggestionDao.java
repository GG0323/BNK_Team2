package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;

@Mapper
public interface IApporvedSuggestionDao {

	public List<ApprovedSuggestionDetailDto> approvedList(long employee_no);

	public ApprovedSuggestionDetailDto approvedDetail(long suggestion_no);

	public List<ApprovedSuggestionDetailDto> approvedSuggestionList();
	
	public ApprovedSuggestionDetailDto selectApprovedSug(@Param("suggestion_no") long suggestion_no);
	
	public int updateAprToCondition(@Param("suggestion_no") long suggestion_no, 
									@Param("condition_no") long condition_no);
	
	public int updateAprToDescription(@Param("suggestion_no") long suggestion_no,
									  @Param("description_no")long description_no);
	public int updateAprToRate(@Param("suggestion_no") long suggestion_no,
							   @Param("rate_no") long rate_no);

	public List<ApprovedSuggestionDetailDto> chooseApprovedList();											
}
