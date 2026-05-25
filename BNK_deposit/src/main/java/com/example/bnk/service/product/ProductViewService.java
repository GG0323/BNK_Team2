package com.example.bnk.service.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dao.product.IProductViewDao;
import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductViewService {

    private final IProductViewDao productViewDao;

    // 상품 목록 조회 + 정렬
    public List<ProductListViewDto> getProductList(String sort) {
        List<ProductListViewDto> list = productViewDao.selectProductList();

        // 최대금리 높은 순
        if ("maxRateDesc".equals(sort)) {
            list.sort(Comparator
            				.comparingDouble(ProductListViewDto::getMax_interest_rate)
            				.reversed()
            );
         // 상품명 순
        } else if ("nameAsc".equals(sort)) {
            list.sort(Comparator
            		.comparing(ProductListViewDto::getProduct_name,
            				Comparator.nullsLast(String::compareTo)));
         
        // 기본금리 높은 순
        } else {
            list.sort(
                Comparator.comparingDouble(ProductListViewDto::getMin_interest_rate)
                          .reversed()
            );
        }
        return list;
    }

    // 상품 상세 조회
    public ProductDetailViewDto getProductDetail(long product_no) {
        return productViewDao.selectProductDetail(product_no);
    }

    // 상품 검색
    public List<ProductListViewDto> searchProductList(String keyword) {

        // 검색어가 비어 있으면 전체 상품 목록 조회
        if (keyword == null || keyword.trim().equals("")) {
            return productViewDao.selectProductList();
        }

        return productViewDao.searchProductList(keyword.trim());
    }

    // 상품 비교 조회
    public List<ProductCompareViewDto> getCompareProducts(String ids) {

        List<Long> productNoList = new ArrayList<>();

        // ids 예시: "1,2,3"
        if (ids == null || ids.trim().equals("")) {
            return new ArrayList<>();
        }

        String[] idArray = ids.split(",");

        for (String id : idArray) {
            try {
                long product_no = Long.parseLong(id.trim());
                productNoList.add(product_no);
            } catch (NumberFormatException e) {
                // 숫자가 아닌 값은 비교 대상에서 제외
            }
        }

        // 비교할 상품이 없으면 빈 리스트 반환
        if (productNoList.size() == 0) {
            return new ArrayList<>();
        }

        return productViewDao.selectCompareProducts(productNoList);
    }
}