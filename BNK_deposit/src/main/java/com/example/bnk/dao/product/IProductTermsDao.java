package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductTermsDto;

@Mapper
public interface IProductTermsDao {

	ProductTermsDto detail(@Param("suggestion_no") long suggestion_no);
	ProductTermsDto selectTermsPrd(@Param("terms_no") long terms_no);
	
	
	
}
