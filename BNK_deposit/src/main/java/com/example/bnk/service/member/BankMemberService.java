package com.example.bnk.service.member;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.utils.AesCryptoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankMemberService {

	private final IBankMemberDao bankMemberDao;
	private final BCryptPasswordEncoder pwEncoder;
	private final AesCryptoUtil aesUtil;
	
	// 회원 정보 검색
    public BankMemberDto getMemberInfo(String loginId) {
    	BankMemberDto dto = bankMemberDao.findMemberById(loginId);
    	if(dto == null) return null;
    	String identifier = aesUtil.decrypt(dto.getMember_identifier());
    	identifier = dto.getMember_type().equals("BUSINESS")
    				? identifier
    				: String.format("%s-%c******", identifier.substring(0, 6), identifier.charAt(6));
    	dto.setMember_identifier(identifier);
        return dto;
    }
    
    // 회원 정보 수정
    public void modifyMemberInfo(BankMemberDto updateDto) {
        bankMemberDao.editMember(updateDto);
    }
    
    // 회원 비밀번호 수정
    public boolean changePassword(String loginId, String currentPassword, String newPassword) {
        BankMemberDto member = bankMemberDao.findMemberById(loginId);
        
        // 1. 회원 정보가 존재하고, 입력한 현재 비밀번호가 DB의 비밀번호와 일치하는지 확인
        if (member != null && pwEncoder.encode(newPassword).equals(member.getPassword_hash())) {
            // 2. 일치하면 새 비밀번호로 업데이트
            bankMemberDao.updatePassword(loginId, pwEncoder.encode(newPassword));
            return true;
        }
        
        // 3. 일치하지 않으면 실패 처리
        return false;
    }
}
