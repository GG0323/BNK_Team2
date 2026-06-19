package com.example.bnk.service.member;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.utils.AesCryptoUtil;

@Service
@Transactional
public class MemberService {
	
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

	@Autowired
	private IBankMemberDao memberDao;
	
	@Autowired
	private BCryptPasswordEncoder pwEncoder;
	
	@Autowired
	private AesCryptoUtil aesUtil;
	
	@Autowired
	private EmailVerificationService emailVerificationService;
	
	// 회원 등록
	public boolean regist(BankMemberDto dto) {
		String identifier = dto.getMember_identifier();
		String email = emailVerificationService.normalize(dto.getEmail());
		
		if (!isValidEmail(email)) return false;
		if (memberDao.emailCheck(email) > 0) return false;
		if (!emailVerificationService.isSignupVerified(email)) return false;
		
		dto.setEmail(email);
		
		dto.setBirth_date(LocalDate.parse(identifier.substring(0, 6), DateTimeFormatter.ofPattern("yyMMdd")));
		dto.setPassword_hash(pwEncoder.encode(dto.getPassword_hash()));
		dto.setMember_identifier(aesUtil.encrypt(identifier));
		dto.setGender(dto.getMember_type().equals("BUSINESS") ? null : identifier.charAt(6) == '1' ? "M" : "F");
		
		List<String> id_list = memberDao.checkIdentifier();
		for(String id : id_list) {
			if(aesUtil.decrypt(id).equals(identifier)) return false;
		}
		
		boolean registered = memberDao.regist(dto) == 1;
		if (registered) {
			emailVerificationService.consumeSignupVerification(email);
		}
		
		return registered;
	}
	
	// 회원 ID 중복 확인	(true: 사용가능, false: 중복)
	public boolean idCheck(String id) {
		return memberDao.idCheck(id) == 0;
	}
	
	// 회원 이메일 중복 확인	(true: 사용가능, false: 중복 또는 형식 오류)
	public boolean emailCheck(String email) {
		String normalizedEmail = emailVerificationService.normalize(email);
		return isValidEmail(normalizedEmail) && memberDao.emailCheck(normalizedEmail) == 0;
	}
	
	private boolean isValidEmail(String email) {
		return email != null && EMAIL_PATTERN.matcher(email).matches();
	}
}
