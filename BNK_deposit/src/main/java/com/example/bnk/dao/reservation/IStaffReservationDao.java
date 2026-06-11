package com.example.bnk.dao.reservation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IStaffReservationDao {

    // 예약 목록 조회
    List<Map<String, Object>> findReservations(Map<String, Object> params);

    // 본 테이블 상태 업데이트
    int updateReservationStatus(Map<String, Object> params);

    // 처리 로그 INSERT
    int insertReservationLog(Map<String, Object> params);
}