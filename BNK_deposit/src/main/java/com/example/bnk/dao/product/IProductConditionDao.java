package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductConditionDto;

@Mapper
public interface IProductConditionDao {
	public int insertAllCondition(ProductConditionDto prdCndDto);
	public int updateProductCondition(ProductConditionDto prdCndDto);
	public ProductConditionDto selectConditionPrd(@Param("condition_no") long condition_no);
}
