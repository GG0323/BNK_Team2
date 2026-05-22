package com.example.bnk.dao.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.QrLoginMemberDto;

@Mapper
public interface IQrLoginDao {

    // QR 로그인용 회원 조회
    // 실제 서비스에서는 앱 로그인 사용자의 member_no를 받아 조회함
    public QrLoginMemberDto selectQrLoginMember(@Param("member_no") long member_no);
}