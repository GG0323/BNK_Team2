package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.MemberProductDto;
import com.example.bnk.dto.product.ProductJoinDraftDto;

@Mapper
public interface IProductSalesDao {

	// 마이페이지용 가입 상품 총 개수 조회
    int countProductSalesByMemberNo(long memberNo);

    // ✨ 특정 회원이 가입한 상품 목록 조회 (JOIN 쿼리 호출용)
    List<MemberProductDto> findSubscribedProductsByMemberNo(String username);
    
    // 커뮤니티 가입 시 우대금리 상승 인터페이스
    public int upTermsCommunityRegist(@Param("member_no") long member_no);
    
    // 회원의 가입 상품 데이터 불러오기
    public MemberProductDto selectUsersProduct(@Param("member_no") long member_no, @Param("account_no") long account_no);

    ProductJoinDraftDto findDraftByMemberAndProduct(
            @Param("memberNo") long memberNo,
            @Param("productNo") long productNo
    );

    ProductJoinDraftDto findBySubscriptionNo(
            @Param("memberNo") long memberNo,
            @Param("subscriptionNo") long subscriptionNo
    );

    int insertDraft(
            @Param("memberNo") long memberNo,
            @Param("productNo") long productNo,
            @Param("linkedAccountId") Long linkedAccountId
    );

    int updateDraftTerms(
            @Param("memberNo") long memberNo,
            @Param("subscriptionNo") long subscriptionNo,
            @Param("subscriptionAmount") long subscriptionAmount,
            @Param("subscriptionMonths") long subscriptionMonths,
            @Param("requiredTermsAgreed") int requiredTermsAgreed,
            @Param("optionalTermsAgreed") int optionalTermsAgreed
    );

    int completeDraft(
            @Param("memberNo") long memberNo,
            @Param("subscriptionNo") long subscriptionNo,
            @Param("accountNo") long accountNo,
            @Param("appliedInterestRate") double appliedInterestRate,
            @Param("maturityDate") java.time.LocalDate maturityDate
    );
}
