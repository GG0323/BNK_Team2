package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductRateDto;

@Mapper
public interface IProductRateDao {
	public int insertAllRate(ProductRateDto prdRateDto);
	public int updateProductRate(ProductRateDto prdRateDto);
	public ProductRateDto selectRatePrd(@Param("rate_no") long rate_no);
}
