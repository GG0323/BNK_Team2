package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberPageController {
	
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/loginPage")
	public String loginPage() {
		return "/member/loginpage";
	}
	
	@GetMapping(value="/loginPage", params="message")
	public String loginFail(Model model, @RequestParam("message") String msg) {
		model.addAttribute("msg", msg);
		return "/member/loginpage";
	}
	
	@GetMapping("/signupPage")
	public String regist() {
		return "/member/signup";
	}
	
}
