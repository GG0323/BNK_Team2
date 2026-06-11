package com.example.bnk.service.reservation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.reservation.IStaffReservationDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffReservationService {

    private final IStaffReservationDao staffResDao;

    // 목록 조회
    public List<?> getReservations(Long branchId, String status, String date) {
        Map<String, Object> params = new HashMap<>();
        params.put("branchId", branchId);
        params.put("status",   status);
        params.put("date",     date);
        return staffResDao.findReservations(params);
    }

    // 확정
    @Transactional
    public int confirm(long reservationId, long employeeNo, String reason) {
        Map<String, Object> p = buildParams(
                reservationId, employeeNo, "CONFIRM", "CONFIRMED",
                reason.isEmpty() ? null : reason, null, null);
        int result = staffResDao.updateReservationStatus(p);
        if (result > 0) staffResDao.insertReservationLog(p);
        return result;
    }

    // 매장 사정 변경
    @Transactional
    public int reassign(long reservationId, long employeeNo, String reason,
                        long newBranchId, LocalDateTime newReservedAt) {
        Date newDt = Date.from(newReservedAt.atZone(ZoneId.systemDefault()).toInstant());
        Map<String, Object> p = buildParams(
                reservationId, employeeNo, "REASSIGN", "REASSIGNED",
                reason, newBranchId, newDt);
        int result = staffResDao.updateReservationStatus(p);
        if (result > 0) staffResDao.insertReservationLog(p);
        return result;
    }

    // 거절
    @Transactional
    public int reject(long reservationId, long employeeNo, String reason) {
        Map<String, Object> p = buildParams(
                reservationId, employeeNo, "REJECT", "REJECTED",
                reason, null, null);
        int result = staffResDao.updateReservationStatus(p);
        if (result > 0) staffResDao.insertReservationLog(p);
        return result;
    }

    // 공통 파라미터 맵 생성
    private Map<String, Object> buildParams(
            long reservationId, long employeeNo,
            String action, String afterStatus,
            String reason, Object newBranchId, Object newReservedAt) {
        Map<String, Object> p = new HashMap<>();
        p.put("reservationId",  reservationId);
        p.put("employeeNo",     employeeNo);
        p.put("action",         action);
        p.put("afterStatus",    afterStatus);
        p.put("reason",         reason);
        p.put("newBranchId",    newBranchId);
        p.put("newReservedAt",  newReservedAt);
        return p;
    }
}