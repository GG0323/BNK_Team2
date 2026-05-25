package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductSuggestionDto;


@Mapper
public interface IProductSuggestionDao {
	
	public List<ProductSuggestionDto> showPrdSugt();

	public int writeSuggestion(@Param("dto")ProductSuggestionDto suggestionDto);
	
	
}
