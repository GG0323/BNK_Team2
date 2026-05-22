package com.example.bnk.dao.product;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.member.MemberProductDto;

@Mapper
public interface IProductSalesDao {

	// 마이페이지용 가입 상품 총 개수 조회
    int countProductSalesByMemberNo(long memberNo);

    // ✨ 특정 회원이 가입한 상품 목록 조회 (JOIN 쿼리 호출용)
    List<MemberProductDto> findSubscribedProductsByMemberNo(long memberNo);

}
