package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductDetailResponseDto;

@Mapper
public interface IProductDetailResponseDao {
	public ProductDetailResponseDto selectProductDetail(@Param("product_no")Long product_no);
}
