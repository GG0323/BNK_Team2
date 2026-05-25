package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.product.ProductRateDto;

@Mapper
public interface IProductRateDao {
	public int insertAllRate(ProductRateDto prdRateDto);
}
