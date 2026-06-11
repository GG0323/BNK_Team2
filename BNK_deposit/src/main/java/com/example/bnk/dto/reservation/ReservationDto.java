package com.example.bnk.dto.reservation;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor                          // 영업점 예약
public class ReservationDto {
    private long reservation_id;             // 예약 PK
    private long member_no;                  // 예약자 FK
    private long branch_id;                  // 예약 지점 FK (TB_BRANCH.branch_id)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reserved_at;       // 예약 방문 일시 (시·분 포함)

    private String biz_type;                 // 업무 유형 (DEPOSIT/LOAN/CARD/FX/ETC)
    private String purpose;                  // 방문 목적 (자유 작성)
    private String status;                   // 예약 상태 (PENDING/CONFIRMED/REASSIGNED/REJECTED/CANCELED)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created_at;        // 신청 일시

    private LocalDateTime updated_at;        // 최근 처리 일시

    // 조회 시 조인해서 함께 내려줄 표시용 필드 (테이블 컬럼 아님)
    private String member_name;              // 회원명 (TB_BANK_MEMBER 조인)
    private String branch_name;              // 영업점명 (TB_BRANCH 조인)
}