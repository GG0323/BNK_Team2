package com.example.bnk.controller.api.employee.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/suggestion")
public class SuggestionApiController {
	
	@Autowired
	//private 
	
	
	@GetMapping("/managers")
	public void managers() {
		
		// 상사 목록을 반환한다. 시큐리티 세션영역의 유저id를 이용해 pk와 유저 이름을 반환한다.
	}
	
	
	
	
	
	
	
}
