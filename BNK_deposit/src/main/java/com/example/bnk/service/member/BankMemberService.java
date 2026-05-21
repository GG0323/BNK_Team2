package com.example.bnk.service.member;

import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dto.member.BankMemberDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankMemberService {

	private final IBankMemberDao bankMemberDao;

    public BankMemberDto getMemberInfo(String loginId) {
        return bankMemberDao.findMemberById(loginId);
    }
    
    public void modifyMemberInfo(BankMemberDto updateDto) {
        bankMemberDao.updateMemberInfo(updateDto);
    }
    
    public boolean changePassword(String loginId, String currentPassword, String newPassword) {
        BankMemberDto member = bankMemberDao.findMemberById(loginId);
        
        // 1. 회원 정보가 존재하고, 입력한 현재 비밀번호가 DB의 비밀번호(평문)와 일치하는지 확인
        if (member != null && currentPassword.equals(member.getPassword_hash())) {
            // 2. 일치하면 새 비밀번호로 업데이트
            bankMemberDao.updatePassword(loginId, newPassword);
            return true;
        }
        
        // 3. 일치하지 않으면 실패 처리
        return false;
    }
}
