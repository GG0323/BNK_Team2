package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.MyProductDto;

@Mapper
public interface IMyProductDao {

    // 로그인 회원의 가입상품 목록 조회
    List<MyProductDto> findMyProductsByMemberNo(@Param("memberNo") long memberNo);

    // 로그인 회원의 특정 가입상품 상세 조회
    MyProductDto findMyProductDetail(
        @Param("memberNo") long memberNo,
        @Param("subscriptionNo") int subscriptionNo
    );
}