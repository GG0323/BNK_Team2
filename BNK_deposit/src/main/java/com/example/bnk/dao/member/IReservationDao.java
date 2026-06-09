package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.common.ReservationDto;

@Mapper
public interface IReservationDao {

    // 예약 등록
    int insertReservation(@Param("dto") ReservationDto dto);

    // 내 예약 목록 조회 (member_no 기준)
    List<ReservationDto> findByMemberNo(@Param("memberNo") long memberNo);

    // 예약 단건 조회
    ReservationDto findById(@Param("reservationId") long reservationId);

    // 예약 취소 (본인 예약 + PENDING/CONFIRMED 상태일 때만)
    int cancelReservation(
            @Param("reservationId") long reservationId,
            @Param("memberNo") long memberNo
    );
}