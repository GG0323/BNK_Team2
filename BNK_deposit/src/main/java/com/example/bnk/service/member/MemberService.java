package com.example.bnk.service.member;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
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

	@Autowired
	private IBankMemberDao memberDao;
	
	@Autowired
	private BCryptPasswordEncoder pwEncoder;
	
	@Autowired
	private AesCryptoUtil aesUtil;
	
	// 회원 등록
	public boolean regist(BankMemberDto dto) {
	    String identifier = dto.getMember_identifier();

	    if ("PERSONAL".equals(dto.getMember_type())) {

	        // 주민등록번호는 숫자만 사용
	        String cleanIdentifier = identifier.replaceAll("[^0-9]", "");

	        if (!cleanIdentifier.matches("\\d{13}")) {
	            throw new IllegalArgumentException("주민등록번호 형식이 올바르지 않습니다.");
	        }

	        String birthText = cleanIdentifier.substring(0, 6);
	        char genderCode = cleanIdentifier.charAt(6);

	        int yy = Integer.parseInt(birthText.substring(0, 2));
	        int mm = Integer.parseInt(birthText.substring(2, 4));
	        int dd = Integer.parseInt(birthText.substring(4, 6));

	        int century;

	        if (genderCode == '1' || genderCode == '2' || genderCode == '5' || genderCode == '6') {
	            century = 1900;
	        } else if (genderCode == '3' || genderCode == '4' || genderCode == '7' || genderCode == '8') {
	            century = 2000;
	        } else {
	            throw new IllegalArgumentException("주민등록번호 성별 코드가 올바르지 않습니다.");
	        }

	        dto.setBirth_date(LocalDate.of(century + yy, mm, dd));

	        if (genderCode == '1' || genderCode == '3' || genderCode == '5' || genderCode == '7') {
	            dto.setGender("M");
	        } else {
	            dto.setGender("F");
	        }

	        dto.setMember_identifier(cleanIdentifier);
	    }

	    else if ("BUSINESS".equals(dto.getMember_type())) {

	        if (!identifier.matches("\\d{3}-\\d{2}-\\d{5}")) {
	            throw new IllegalArgumentException("사업자등록번호 형식이 올바르지 않습니다.");
	        }

	        if (dto.getBirth_date() == null) {
	            throw new IllegalArgumentException("개업일자를 입력해주세요.");
	        }

	        // 기업회원은 성별 없음
	        dto.setGender(null);
	    }

	    else {
	        throw new IllegalArgumentException("회원구분이 올바르지 않습니다.");
	    }

	    dto.setPassword_hash(pwEncoder.encode(dto.getPassword_hash()));
	    dto.setMember_identifier(aesUtil.encrypt(dto.getMember_identifier()));

	    return memberDao.regist(dto) == 1;
	}
	
	// 회원 ID 중복 확인	(true: 사용가능, false: 중복)
	public boolean idCheck(String id) {
		return memberDao.idCheck(id) == 0;
	}
}
