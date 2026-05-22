package com.example.bnk.service.member;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dto.member.BankMemberDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankMemberService {

	private final IBankMemberDao bankMemberDao;
	private final BCryptPasswordEncoder passwordEncoder;

    public BankMemberDto getMemberInfo(String loginId) {
        return bankMemberDao.findMemberById(loginId);
    }
    
    public void modifyMemberInfo(BankMemberDto updateDto) {
        bankMemberDao.editMember(updateDto);
    }
    
    public boolean changePassword(String loginId, String currentPassword, String newPassword) {
        BankMemberDto member = bankMemberDao.findMemberById(loginId);
        
        // 회원 존재 확인 & ✨ 암호화된 비밀번호와 평문 비밀번호를 matches()로 비교!
        if (member != null && passwordEncoder.matches(currentPassword, member.getPassword_hash())) {
            
            // 일치하면 '새 비밀번호'도 암호화해서 DB에 업데이트!
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            bankMemberDao.updatePassword(loginId, encodedNewPassword);
            return true;
        }
        
        // 일치하지 않으면 실패처리
        return false;
    }
}
