package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductDto;

@Mapper
public interface IProductDao {
	public List<ProductDto> showProduct();
	public List<ProductDto> showPrdToDeposit();
	public List<ProductDto> showPrdToSavings();
	public ProductDto showProductDetails(@Param("product_no") int product_no);
}
