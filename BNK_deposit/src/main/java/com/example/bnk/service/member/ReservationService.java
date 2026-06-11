package com.example.bnk.service.member;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dao.reservation.IReservationDao;
import com.example.bnk.dto.reservation.ReservationDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final IReservationDao reservationDao;

    // 예약 등록
    public int createReservation(ReservationDto dto) {
        return reservationDao.insertReservation(dto);
    }

    // 내 예약 목록 조회
    public List<ReservationDto> getMyReservations(long memberNo) {
        return reservationDao.findByMemberNo(memberNo);
    }

    // 예약 취소 (본인 것만)
    public boolean cancelReservation(long reservationId, long memberNo) {
        int result = reservationDao.cancelReservation(reservationId, memberNo);
        return result > 0;
    }
    
    // 예약된 시간대 조회
    public List<String> getBookedSlots(long branchId, String date) {
        return reservationDao.findBookedSlots(branchId, date);
    }
}