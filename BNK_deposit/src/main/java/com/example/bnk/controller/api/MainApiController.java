package com.example.bnk.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.MemberService;

@RestController
@RequestMapping("/api")
public class MainApiController {
	@Autowired
	private MemberService service;
	
	@PostMapping("/2/member")
	public ResponseEntity<Boolean> signup(BankMemberDto dto){
		return ResponseEntity.ok(service.regist(dto));
	}
	
	@GetMapping("/1/auth")
	public ResponseEntity<Boolean> checkAuth(@AuthenticationPrincipal String username){
		return ResponseEntity.ok("anonymousUser".equals(username));
	}

}
