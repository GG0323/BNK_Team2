package com.example.bnk.service.product;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.product.IProductViewDao;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductViewService {

    private final IProductViewDao productViewDao;

    // 상품 목록 조회 + 정렬
    public List<ProductListViewDto> getProductList(String sort) {
        return getProductList(sort, "ALL");
    }

    // 상품 목록 조회 + 카테고리 필터 + 정렬
    public List<ProductListViewDto> getProductList(String sort, String productType) {
        List<ProductListViewDto> list = productViewDao.selectProductList();

        filterProductListByType(list, productType);
        sortProductList(list, sort);

        return list;
    }

    // 상품 상세 조회
    public ProductDetailViewDto getProductDetail(long product_no) {
        return productViewDao.selectProductDetail(product_no);
    }

    // 상품 검색
    public List<ProductListViewDto> searchProductList(String keyword) {
        return searchProductList(keyword, "baseRateDesc", "ALL");
    }

    // 상품 검색 + 카테고리 필터 + 정렬
    public List<ProductListViewDto> searchProductList(String keyword, String sort, String productType) {

        List<ProductListViewDto> list;

        if (keyword == null || keyword.trim().equals("")) {
            list = productViewDao.selectProductList();
        } else {
            list = productViewDao.searchProductList(keyword.trim());
        }

        filterProductListByType(list, productType);
        sortProductList(list, sort);

        return list;
    }

    // 비회원/공통 추천 상품 TOP 3
    // 기준: DRAFT 제외 가입 이력 + 판매중/판매기간 유효 상품
    public List<ProductListViewDto> getPopularRecommendedProducts() {
        return productViewDao.selectPopularRecommendedProducts();
    }

    // 로그인 회원 맞춤 추천 상품 TOP 3
    // 추천 불가능한 회원이면 비회원 인기 상품으로 대체
    public List<ProductListViewDto> getRecommendedProductsForMember(BankMemberDto member) {

        if (!isPersonalRecommendationTarget(member)) {
            return getPopularRecommendedProducts();
        }

        int age = calculateAge(member.getBirth_date());
        String groupCode = getMemberGroupCode(age, member.getGender());

        if (groupCode == null) {
            return getPopularRecommendedProducts();
        }

        List<ProductListViewDto> recommendedList =
                productViewDao.selectRecommendedProductsForMember(
                        age,
                        member.getGender(),
                        member.getMember_type(),
                        groupCode
                );

        if (recommendedList == null || recommendedList.isEmpty()) {
            return getPopularRecommendedProducts();
        }

        return recommendedList;
    }

    // 기존 /products/recommend 테스트 URL 호환용
    // 현재는 비회원 인기 추천으로 동작시킴
    public List<ProductListViewDto> getRecommendProductList() {
        return getPopularRecommendedProducts();
    }

    // 회원 추천 가능 여부
    public boolean isPersonalRecommendationTarget(BankMemberDto member) {
        if (member == null) return false;
        if (!"PERSONAL".equals(member.getMember_type())) return false;
        if (member.getBirth_date() == null) return false;
        if (member.getGender() == null || member.getGender().trim().equals("")) return false;

        String status = member.getMember_status();

        return "ASSOCIATE".equals(status) || "REGULAR".equals(status);
    }

    // 화면 문구용 추천 유형
    public String getRecommendationMode(BankMemberDto member) {
        return isPersonalRecommendationTarget(member) ? "MEMBER" : "POPULAR";
    }

    // 화면 문구용 추천 메시지
    public String getRecommendationMessage(BankMemberDto member) {
        if (!isPersonalRecommendationTarget(member)) {
            return "지금 고객들이 가장 많이 선택한 인기 예적금 상품입니다.";
        }

        int age = calculateAge(member.getBirth_date());
        String groupCode = getMemberGroupCode(age, member.getGender());

        if ("YOUTH".equals(groupCode)) {
            return "청년층 고객의 가입 가능 조건과 인기 흐름을 반영한 추천 상품입니다.";
        }

        if ("MIDDLE_MALE".equals(groupCode)) {
            return "중장년층 남성 고객의 가입 가능 조건과 인기 흐름을 반영한 추천 상품입니다.";
        }

        if ("MIDDLE_FEMALE".equals(groupCode)) {
            return "중장년층 여성 고객의 가입 가능 조건과 인기 흐름을 반영한 추천 상품입니다.";
        }

        if ("SENIOR".equals(groupCode)) {
            return "노년층 고객의 가입 가능 조건과 인기 흐름을 반영한 추천 상품입니다.";
        }

        return "회원님의 가입 가능 조건을 반영한 추천 상품입니다.";
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String getMemberGroupCode(int age, String gender) {
        if (age >= 19 && age <= 39) {
            return "YOUTH";
        }

        if (age >= 40 && age <= 64) {
            if ("M".equals(gender)) return "MIDDLE_MALE";
            if ("F".equals(gender)) return "MIDDLE_FEMALE";
            return null;
        }

        if (age >= 65) {
            return "SENIOR";
        }

        return null;
    }

    // 상품 비교 조회
    public List<ProductCompareViewDto> getCompareProducts(String ids) {

        List<Long> productNoList = new ArrayList<>();

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

        if (productNoList.size() == 0) {
            return new ArrayList<>();
        }

        return productViewDao.selectCompareProducts(productNoList);
    }

    // 로그인 회원 유형별 상품 목록 조회
    public List<ProductListViewDto> getProductListForMember(String memberType, String sort) {
        return getProductListForMember(memberType, sort, "ALL");
    }

    // 로그인 회원 유형별 상품 목록 조회 + 카테고리 필터 + 정렬
    public List<ProductListViewDto> getProductListForMember(String memberType, String sort, String productType) {

        List<ProductListViewDto> list =
                productViewDao.selectProductListForMember(memberType);

        filterProductListByType(list, productType);
        sortProductList(list, sort);

        return list;
    }

    // 로그인 회원 유형별 상품 검색
    public List<ProductListViewDto> searchProductListForMember(String memberType, String keyword) {
        return searchProductListForMember(memberType, keyword, "baseRateDesc", "ALL");
    }

    // 로그인 회원 유형별 상품 검색 + 카테고리 필터 + 정렬
    public List<ProductListViewDto> searchProductListForMember(String memberType,
                                                               String keyword,
                                                               String sort,
                                                               String productType) {

        List<ProductListViewDto> list;

        if (keyword == null || keyword.trim().equals("")) {
            list = productViewDao.selectProductListForMember(memberType);
        } else {
            list = productViewDao.searchProductListForMember(
                    memberType,
                    keyword.trim()
            );
        }

        filterProductListByType(list, productType);
        sortProductList(list, sort);

        return list;
    }

    // productType 필터
    // ALL이면 전체, DEPOSIT이면 예금, SAVINGS면 적금
    private void filterProductListByType(List<ProductListViewDto> list, String productType) {
        if (list == null) return;

        String normalizedProductType = normalizeProductType(productType);

        if ("ALL".equals(normalizedProductType)) {
            return;
        }

        list.removeIf(product ->
                !normalizedProductType.equals(product.getProduct_type())
        );
    }

    private String normalizeProductType(String productType) {
        if ("DEPOSIT".equals(productType)) {
            return "DEPOSIT";
        }

        if ("SAVINGS".equals(productType)) {
            return "SAVINGS";
        }

        return "ALL";
    }

    private void sortProductList(List<ProductListViewDto> list, String sort) {
        if (list == null) return;

        if ("maxRateDesc".equals(sort)) {
            list.sort(Comparator
                    .comparingDouble(ProductListViewDto::getMax_interest_rate)
                    .reversed());

        } else if ("nameAsc".equals(sort)) {
            list.sort(Comparator
                    .comparing(
                            ProductListViewDto::getProduct_name,
                            Comparator.nullsLast(String::compareTo)
                    ));

        } else {
            list.sort(Comparator
                    .comparingDouble(ProductListViewDto::getMin_interest_rate)
                    .reversed());
        }
    }
}