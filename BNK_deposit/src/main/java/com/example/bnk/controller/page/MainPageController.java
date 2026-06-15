package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin("*")
@Controller
public class MainPageController {
	
	// 고객 메인 페이지
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	// 고객 로그인 페이지
	@GetMapping("/loginPage")
	public String loginPage(Model model,
	        @RequestParam(value = "message", required = false) String msg) {
	    if (msg != null) {
	        model.addAttribute("msg", "로그아웃 되었습니다.");
	    }

	    return "/member/loginpage";
	}
	
	// 고객 회원가입 페이지
	@GetMapping("/signupPage")
	public String regist() {
		return "/member/signup";
	}
}
