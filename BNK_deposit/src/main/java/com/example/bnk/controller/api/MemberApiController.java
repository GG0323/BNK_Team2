package com.example.bnk.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberApiController {
	@Autowired
	private MemberService service;
	
	@GetMapping("/1/member")
	public ResponseEntity<Boolean> signup(BankMemberDto dto){
		return ResponseEntity.ok(service.regist(dto));
	}

}
