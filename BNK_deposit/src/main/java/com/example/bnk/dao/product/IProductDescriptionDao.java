package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.product.ProductDescriptionDto;

@Mapper
public interface IProductDescriptionDao {
	public int insertAllDescription(ProductDescriptionDto prdDescDto);
}
