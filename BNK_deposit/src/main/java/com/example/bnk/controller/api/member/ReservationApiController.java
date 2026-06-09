package com.example.bnk.controller.api.member;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.common.ReservationDto;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.member.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member/reservation")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;
    private final BankMemberService bankMemberService;

    // 내 예약 목록 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> myReservations(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        BankMemberDto memberInfo = bankMemberService.getMemberInfo(principal.getName());
        if (memberInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        List<ReservationDto> list = reservationService.getMyReservations(memberInfo.getMember_no());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // 예약 등록
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> create(
            Principal principal,
            @RequestParam("branchId") long branchId,
            @RequestParam("reservedAt") String reservedAt,
            @RequestParam("bizType") String bizType,
            @RequestParam(value = "purpose", defaultValue = "") String purpose) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        BankMemberDto memberInfo = bankMemberService.getMemberInfo(principal.getName());
        if (memberInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        // 업무 유형 백엔드 검증
        if (!bizType.matches("DEPOSIT|LOAN|CARD|FX|ETC")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("업무 유형이 올바르지 않습니다."));
        }

        // 예약 일시 파싱 (datetime-local: yyyy-MM-dd'T'HH:mm)
        LocalDateTime reservedDateTime;
        try {
            reservedDateTime = LocalDateTime.parse(reservedAt);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("예약 일시 형식이 올바르지 않습니다."));
        }

        // 과거 시각 예약 차단
        if (reservedDateTime.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("지난 시간으로는 예약할 수 없습니다."));
        }

        ReservationDto dto = new ReservationDto();
        dto.setMember_no(memberInfo.getMember_no());
        dto.setBranch_id(branchId);
        dto.setReserved_at(reservedDateTime);
        dto.setBiz_type(bizType);
        dto.setPurpose(purpose.trim().isEmpty() ? null : purpose);

        int result = reservationService.createReservation(dto);
        return result > 0
                ? ResponseEntity.ok(ApiResponse.success("예약이 접수되었습니다."))
                : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("예약 접수에 실패했습니다."));
    }

    // 예약 취소
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<?>> cancel(
            Principal principal,
            @RequestParam("reservationId") long reservationId) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        BankMemberDto memberInfo = bankMemberService.getMemberInfo(principal.getName());
        if (memberInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        boolean canceled = reservationService.cancelReservation(reservationId, memberInfo.getMember_no());
        return canceled
                ? ResponseEntity.ok(ApiResponse.success("예약이 취소되었습니다."))
                : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("취소할 수 없는 예약입니다."));
    }
}