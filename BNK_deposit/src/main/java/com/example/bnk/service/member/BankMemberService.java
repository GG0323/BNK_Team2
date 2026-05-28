package com.example.bnk.service.member;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final AccountService accountService;
	
	// 마지막 로그인 저장
	public void updateLastLogin(long mamber_no) {
		bankMemberDao.lastLogin(mamber_no);
	}
	
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
        
        // 회원 정보가 존재하고, 입력한 비밀번호가 DB의 암호화된 비밀번호와 일치하는지 확인
        if (member != null && pwEncoder.matches(currentPassword, member.getPassword_hash())) {
            
            // 일치하면 '새 비밀번호'도 암호화해서 DB에 업데이트!
            String encodedNewPassword = pwEncoder.encode(newPassword);
            bankMemberDao.updatePassword(loginId, encodedNewPassword);
            return true;
        }
        
        // 일치하지 않으면 바로 실패(false) 반환
        return false;
    }
    
    // 마지막 로그인 시간 갱신
    public void updateLastLoginAt(String loginId) {
        bankMemberDao.updateLastLoginAt(loginId);
    }
    
    public boolean updateMemberStatus(String loginId, String memberStatus) {
        int result = bankMemberDao.updateMemberStatus(loginId, memberStatus);
        return result > 0;
    }

    // 휴면으로 전환
    public boolean makeDormant(String loginId) {
        return updateMemberStatus(loginId, "DORMANT");
    }

    // 휴면 계정 인증 후 활성화
    public boolean releaseDormant(String loginId) {

        BankMemberDto member = bankMemberDao.findMemberById(loginId);

        if (member == null) {
            return false;
        }

        boolean hasAccount = !accountService.getAccounts(loginId).isEmpty();

        String nextStatus = hasAccount ? "REGULAR" : "ASSOCIATE";

        return updateMemberStatus(loginId, nextStatus);
    }

    // 회원 탈퇴
    public boolean withdrawMember(String loginId) {
        return updateMemberStatus(loginId, "WITHDRAWN");
    }
}
