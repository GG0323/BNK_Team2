package com.example.bnk.service.member;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IBranchDao;
import com.example.bnk.dto.common.BranchDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final IBranchDao branchDao;

    // 예약 가능한 영업점 목록
    public List<BranchDto> getActiveBranches() {
        return branchDao.findActiveBranches();
    }
}