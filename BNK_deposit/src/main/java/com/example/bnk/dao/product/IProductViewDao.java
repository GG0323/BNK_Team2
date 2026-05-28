package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;

@Mapper
public interface IProductViewDao {

    // 판매중인 예금/적금 상품 목록 조회
    // 조건: product_status = 'SALE'
    // 조건: product_type IN ('DEPOSIT', 'SAVINGS')
    public List<ProductListViewDto> selectProductList();

    // 상품 상세 조회
    // TB_PRODUCT + TB_PRODUCT_DESCRIPTION + TB_PRODUCT_CONDITION
    public ProductDetailViewDto selectProductDetail(@Param("product_no") long product_no);

    // 키워드 기반 상품 검색
    // TB_KEYWORD.normalized_keyword 활용
    public List<ProductListViewDto> searchProductList(@Param("keyword") String keyword);

    // 상품 비교 조회
    // 선택한 상품 번호 여러 개를 기준으로 비교 데이터 조회
    public List<ProductCompareViewDto> selectCompareProducts(@Param("product_no_list") List<Long> product_no_list);
    
    // 회원 유형별 상품 목록 조회
    List<ProductListViewDto> selectProductListForMember(
            @Param("memberType") String memberType
    );

    // 회원 유형별 상품 목록 검색
    List<ProductListViewDto> searchProductListForMember(
            @Param("memberType") String memberType,
            @Param("keyword") String keyword
    );
}