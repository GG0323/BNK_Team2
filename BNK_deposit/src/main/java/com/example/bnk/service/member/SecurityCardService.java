package com.example.bnk.service.member;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.ISecurityCardDao;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.member.SecurityCardDto;
import com.example.bnk.utils.AesCryptoUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SecurityCardService {

	private final ISecurityCardDao securityCardDao;
	private final AesCryptoUtil aesUtil;
	private final EmailService emailService;
	private final SecureRandom secureRandom = new java.security.SecureRandom();

	// 준회원 가입 성공 후 보안카드 생성, 암호화 저장, 이메일 발송
	public void issueSignupSecurityCard(BankMemberDto member) {
		if(member == null || member.getMember_no() <= 0) {
			throw new IllegalArgumentException("보안카드를 발급할 회원 정보가 없습니다.");
		}

		String plainSecurityCardNumber = generateSecurityCardNumber();
		String encryptedSecurityCardNumber = aesUtil.encryptGcmToString(plainSecurityCardNumber);

		SecurityCardDto securityCard = new SecurityCardDto();
		securityCard.setMember_no(member.getMember_no());
		securityCard.setSec_num(encryptedSecurityCardNumber);

		securityCardDao.insertSecurityCard(securityCard);
		
		if(securityCard.getSec_no() <= 0) {
			throw new IllegalStateException("보안카드 번호 생성에 실패했습니다.");
		}
		
		emailService.sendSecurityCard(member.getEmail(), plainSecurityCardNumber, securityCard.getSec_no());
	}

	// 0~99 사이의 랜덤값 20개를 두 자리 문자열로 이어붙여 보안카드 번호 생성
	private String generateSecurityCardNumber() {
		StringBuilder number = new StringBuilder(20 * 3);

		for(int i = 0, randomNum; i < 20; i++) {
			randomNum = secureRandom.nextInt(100);
			
			if(randomNum < 10) number.append("0");
			
			number.append(randomNum + " ");
		}
		return number.toString();
	}
}
