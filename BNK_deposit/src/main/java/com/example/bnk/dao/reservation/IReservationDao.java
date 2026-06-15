package com.example.bnk.dao.reservation;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.reservation.ReservationDto;

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
    
    // 특정 영업점·날짜의 예약된 시간대 조회 (마감 판단용)
    List<String> findBookedSlots(
        @Param("branchId") long branchId,
        @Param("date") String date   // yyyy-MM-dd
    );
}