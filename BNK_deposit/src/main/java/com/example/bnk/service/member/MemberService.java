package com.example.bnk.service.member;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
		
		dto.setBirth_date(LocalDate.parse(identifier.substring(0, 6), DateTimeFormatter.ofPattern("yyMMdd")));
		dto.setPassword_hash(pwEncoder.encode(dto.getPassword_hash()));
		dto.setMember_identifier(aesUtil.encrypt(identifier));
		dto.setGender(dto.getMember_type().equals("BUSINESS") ? null : identifier.charAt(6) == '1' ? "M" : "F");
		
		List<String> id_list = memberDao.checkIdentifier();
		for(String id : id_list) {
			if(aesUtil.decrypt(id).equals(identifier)) return false;
		}
		
		return memberDao.regist(dto) == 1;
	}
	
	// 회원 ID 중복 확인	(true: 사용가능, false: 중복)
	public boolean idCheck(String id) {
		return memberDao.idCheck(id) == 0;
	}
}