package com.example.bnk.controller.api.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.service.member.BankMemberLogService;

@RestController
@RequestMapping("/api/log/member")
public class BankMemberLogApiController {
	
	@Autowired
	private BankMemberLogService logService;
	
	
	
	
	
}
