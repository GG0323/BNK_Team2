package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.BankMemberLogDto;
import com.example.bnk.service.member.BankMemberLogService;

@RestController
@RequestMapping("/api/log/member")
public class BankMemberLogApiController {
	
	@Autowired
	private BankMemberLogService logService;
	
	@GetMapping("/allList")
	public List<BankMemberLogDto> allMemberLogs(){
		
		List<BankMemberLogDto> list = logService.allLog();
		
		return list;
	}
	
	
	
}


