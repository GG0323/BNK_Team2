package com.example.bnk.controller.api.reservation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.reservation.BranchDto;
import com.example.bnk.service.reservation.BranchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member/reservation")
@RequiredArgsConstructor
public class BranchApiController {

    private final BranchService branchService;

    // 영업점 목록 조회 (지도·목록 공용)
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<?>> branches() {
        List<BranchDto> list = branchService.getActiveBranches();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}