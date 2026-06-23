package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductTermsViewDto;

@Mapper
public interface IProductTermsViewDao {

    List<ProductTermsViewDto> selectProductTermsByProductNo(@Param("productNo") long productNo);

    int deleteProductTermsByProductRange(@Param("startProductNo") long startProductNo,
                                         @Param("endProductNo") long endProductNo);

    int insertProductTerms(ProductTermsViewDto productTerms);
}