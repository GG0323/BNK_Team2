package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;

@Mapper
public interface IApporvedSuggestionDao {

	List<ApprovedSuggestionDetailDto> approvedList(long employee_no);

	ApprovedSuggestionDetailDto approvedDetail(long suggestion_no);

}
