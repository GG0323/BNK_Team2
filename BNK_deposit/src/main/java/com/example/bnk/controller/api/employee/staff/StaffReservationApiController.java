package com.example.bnk.controller.api.employee.staff;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.reservation.StaffReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff/reservation")
@RequiredArgsConstructor
public class StaffReservationApiController {

    private final StaffReservationService staffReservationService;
    private final EmployeeListService empService;

    // 예약 목록 조회 (필터: 영업점·상태·날짜)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> list(
            @AuthenticationPrincipal String username,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "status",   required = false) String status,
            @RequestParam(value = "date",     required = false) String date) {

        EmployeeDto emp = empService.findByUsername(username);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        List<?> list = staffReservationService.getReservations(branchId, status, date);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // 확정
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirm(
            @AuthenticationPrincipal String username,
            @RequestParam("reservationId") long reservationId,
            @RequestParam(value = "reason", defaultValue = "") String reason) {

        EmployeeDto emp = empService.findByUsername(username);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        int result = staffReservationService.confirm(reservationId, emp.getEmployee_no(), reason);
        return result > 0
                ? ResponseEntity.ok(ApiResponse.success("예약이 확정되었습니다."))
                : ResponseEntity.badRequest().body(ApiResponse.fail("처리에 실패했습니다."));
    }

    // 매장 사정 변경 (REASSIGN)
    @PostMapping("/reassign")
    public ResponseEntity<ApiResponse<?>> reassign(
            @AuthenticationPrincipal String username,
            @RequestParam("reservationId") long reservationId,
            @RequestParam("reason")        String reason,
            @RequestParam("newBranchId")   long newBranchId,
            @RequestParam("newReservedAt") String newReservedAt) {

        EmployeeDto emp = empService.findByUsername(username);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        LocalDateTime newDt;
        try {
            newDt = LocalDateTime.parse(newReservedAt);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("날짜·시간 형식이 올바르지 않습니다."));
        }

        int result = staffReservationService.reassign(
                reservationId, emp.getEmployee_no(), reason, newBranchId, newDt);
        return result > 0
                ? ResponseEntity.ok(ApiResponse.success("변경이 통보되었습니다."))
                : ResponseEntity.badRequest().body(ApiResponse.fail("처리에 실패했습니다."));
    }

    // 거절
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<?>> reject(
            @AuthenticationPrincipal String username,
            @RequestParam("reservationId") long reservationId,
            @RequestParam("reason")        String reason) {

        EmployeeDto emp = empService.findByUsername(username);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        int result = staffReservationService.reject(reservationId, emp.getEmployee_no(), reason);
        return result > 0
                ? ResponseEntity.ok(ApiResponse.success("예약이 거절되었습니다."))
                : ResponseEntity.badRequest().body(ApiResponse.fail("처리에 실패했습니다."));
    }
}