package com.example.bnk.dao.reservation;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.reservation.BranchDto;

@Mapper
public interface IBranchDao {

    // 예약 가능한 영업점 목록 (운영중인 것만)
    List<BranchDto> findActiveBranches();
}