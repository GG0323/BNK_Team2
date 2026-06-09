package com.example.bnk.dao.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.bnk.dto.common.BranchDto;

@Mapper
public interface IBranchDao {

    // 예약 가능한 영업점 목록 (운영중인 것만)
    List<BranchDto> findActiveBranches();
}