package com.example.bnk.dto.reservation;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor                          // 영업점
public class BranchDto {
    private long branch_id;                  // 영업점 PK
    private String branch_name;              // 영업점명
    private String branch_code;              // 영업점 코드
    private String address;                  // 주소
    private String phone_number;             // 대표번호
    private double latitude;                 // 위도 (지도 마커용)
    private double longitude;                // 경도 (지도 마커용)
    private String status;                   // 운영 상태 (ACTIVE/INACTIVE)
    private LocalDate created_at;            // 생성일
    private LocalDate updated_at;            // 수정일
}