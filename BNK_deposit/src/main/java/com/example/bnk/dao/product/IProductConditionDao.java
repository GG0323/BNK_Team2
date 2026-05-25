package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.product.ProductConditionDto;

@Mapper
public interface IProductConditionDao {
	int insertAllCondition(ProductConditionDto prdCndDto);
}
