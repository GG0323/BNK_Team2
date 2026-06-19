package com.example.bnk.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.EmailVerificationService;
import com.example.bnk.service.member.MemberService;

@RestController
@RequestMapping("/api")
public class MainApiController {
	@Autowired
	private MemberService service;
	
	@Autowired
	private EmailVerificationService emailVerificationService;
	
	@PostMapping("/2/member")
	public ResponseEntity<Boolean> signup(BankMemberDto dto){
		return ResponseEntity.ok(service.regist(dto));
	}
	
	// 회원가입 이메일 중복 확인 - /api/member/** 보안 필터를 피하기 위해 MainApiController에 배치
	@GetMapping("/signup/email/check")
	public ResponseEntity<Boolean> emailCheck(@RequestParam("email") String email) {
		return ResponseEntity.ok(service.emailCheck(email));
	}
	
	// 회원가입 이메일 인증번호 발송
	@PostMapping("/signup/email/verification/send")
	public ResponseEntity<Boolean> sendEmailVerificationCode(@RequestParam("email") String email) {
		if (!service.emailCheck(email)) {
			return ResponseEntity.ok(false);
		}
		
		emailVerificationService.sendSignupCode(email);
		return ResponseEntity.ok(true);
	}
	
	// 회원가입 이메일 인증번호 확인
	@PostMapping("/signup/email/verification/confirm")
	public ResponseEntity<Boolean> confirmEmailVerificationCode(
			@RequestParam("email") String email,
			@RequestParam("code") String code) {
		if (!service.emailCheck(email)) {
			return ResponseEntity.ok(false);
		}
		
		return ResponseEntity.ok(emailVerificationService.confirmSignupCode(email, code));
	}
	
	@GetMapping("/1/auth")
	public ResponseEntity<Boolean> checkAuth(@AuthenticationPrincipal String username){
		return ResponseEntity.ok("anonymousUser".equals(username));
	}

}
