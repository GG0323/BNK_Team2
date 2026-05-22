package com.example.bnk.service.product;

import java.util.ArrayList;
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

    // 상품 목록 조회
    public List<ProductListViewDto> getProductList() {
        return productViewDao.selectProductList();
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

    // 사용자 정보 기반 맞춤 상품 추천
    // 현재는 시연용으로 사용자 정보를 하드코딩해서 추천 점수를 계산함
    public List<ProductListViewDto> getRecommendProductList() {

        // 시연용 사용자 정보
        // 추후에는 로그인한 회원의 TB_BANK_MEMBER 정보를 가져와서 사용하면 됨
        int age = 25;
        int creditScore = 850;
        String memberType = "PERSONAL";

        List<ProductListViewDto> productList = productViewDao.selectProductList();

        productList.sort((p1, p2) -> {
            int score1 = getRecommendScore(p1, age, creditScore, memberType);
            int score2 = getRecommendScore(p2, age, creditScore, memberType);

            // 점수가 높은 상품이 먼저 오도록 정렬
            return score2 - score1;
        });

        return productList;
    }

    // 상품별 추천 점수 계산
    private int getRecommendScore(ProductListViewDto product,
                                  int age,
                                  int creditScore,
                                  String memberType) {

        int score = 0;

        String productName = product.getProduct_name();
        String productType = product.getProduct_type();
        String mobileJoinYn = product.getMobile_join_yn();

        // 개인 회원이면 기본 추천 점수
        if ("PERSONAL".equals(memberType)) {
            score += 10;
        }

        // 20~30대면 청년 상품 우선
        if (age >= 19 && age <= 34) {
            if (productName != null && productName.contains("청년")) {
                score += 60;
            }

            if ("SAVINGS".equals(productType)) {
                score += 20;
            }
        }

        // 신용점수가 높은 고객이면 우대/주거래 상품 우선
        if (creditScore >= 800) {
            if (productName != null && productName.contains("우대")) {
                score += 30;
            }

            if (productName != null && productName.contains("주거래")) {
                score += 30;
            }
        }

        // 모바일 가입 가능 상품 우선
        if ("Y".equals(mobileJoinYn)) {
            score += 15;
        }

        // 최고금리가 높은 상품 가산점
        if (product.getMax_interest_rate() >= 4.0) {
            score += 25;
        } else if (product.getMax_interest_rate() >= 3.0) {
            score += 15;
        }

        // 파킹통장 같은 쉬운 접근 상품 가산점
        if (productName != null && productName.contains("파킹")) {
            score += 10;
        }

        return score;
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