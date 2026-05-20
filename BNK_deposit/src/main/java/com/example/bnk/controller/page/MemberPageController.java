package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	

}
