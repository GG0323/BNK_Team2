package com.example.bnk.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberApiController {
	@Autowired
	private MemberService service;
	
	@GetMapping("/1/{id}")
	public ResponseEntity<Boolean> idCheck(@PathVariable("id")String id){
		return ResponseEntity.ok(service.idCheck(id));
	}
	
	@PostMapping("/2/member")
	public ResponseEntity<Boolean> signup(BankMemberDto dto){
		return ResponseEntity.ok(service.regist(dto));
	}
	
}
