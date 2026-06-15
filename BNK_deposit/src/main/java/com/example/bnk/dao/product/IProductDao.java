package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.PendingProductDetailDto;
import com.example.bnk.dto.product.PendingProductListDto;
import com.example.bnk.dto.product.ProductDto;

@Mapper
public interface IProductDao {
	public List<ProductDto> showProduct();
	public List<ProductDto> showPrdToDeposit();
	public List<ProductDto> showPrdToSavings();
	public ProductDto showProductDetails(@Param("product_no") int product_no);
	public int updateProductStatus(ProductDto productDto);
	
	// 상품 등록 (대기)
	public int insertProduct(@Param("dto") ProductDto product);
	// 대기 상품 목록 불러오기
	public List<PendingProductListDto> selectPendingProductList();
	// 대기 상품 상세 불러오기
	public PendingProductDetailDto selectPendingProductDetail(@Param("product_no")Long product_no);
	// 대기 상품 승인하기
	public int approvePendingProduct(@Param("product_no")Long product_no);
	
	
	
}
