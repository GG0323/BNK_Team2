package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.product.ProductConditionDto;

@Mapper
public interface IProductConditionDao {
	public int insertAllCondition(ProductConditionDto prdCndDto);
	public int updateProductCondition(ProductConditionDto prdCndDto);
}
