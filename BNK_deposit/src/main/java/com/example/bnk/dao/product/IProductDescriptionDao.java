package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductDescriptionDto;

@Mapper
public interface IProductDescriptionDao {
	public int insertAllDescription(ProductDescriptionDto prdDescDto);
	public int updateProductDescription(ProductDescriptionDto prdDescDto);
	public ProductDescriptionDto selectDescriptionPrd(@Param("description_no") long description_no);
}
