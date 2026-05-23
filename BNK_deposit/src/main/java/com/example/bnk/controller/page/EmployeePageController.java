package com.example.bnk.controller.page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bnk.auth.EmployeeDetails;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.member.AccountService;

@Controller
@RequestMapping("/employee")
public class EmployeePageController {

    private final AccountService accountService;
	
	@Autowired
	EmployeeLogService logService;

    EmployeePageController(AccountService accountService) {
        this.accountService = accountService;
    }
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	
	
	
	// index 페이지 겸 로그인 페이지  /employee/loginPage
	@GetMapping("/loginPage") 
	public String mainWorkSpace() {
		return "Employees/mainWorkspaceLogin";
	}
	// 로그인 실패
	@GetMapping(value="/loginPage", params="message")
	public String loginFail(Model model, @RequestParam("message") String msg) {
		model.addAttribute("msg", msg);
		return "/Employees/mainWorkspaceLogin";
	}
	
	
	
	
	
	
	// 관리자 페이지, 
	@GetMapping("/manager/managerPage")
	public String managerPage() {
		return "/Employees/manager/managerPage";
	}
	
	// 신규사원 등록 /employee/manager/HRM/hrmRegist
	@GetMapping("/manager/HRM/hrmRegist")  
	public String hrmRegist() {
		logService.build("PAGEVIEW", null, null, "페이지간 이동을 실현한다: 인사관리/신규 사원 등록", "GET", "/employee/manager/HRM/hrmRegist");
		return "Employees/manager/HRM/hrmRegist";
	}
	
	// 직원 로그 조회 페이지 /employee/manager/LOG/logList
	@GetMapping("/manager/LOG/logList")
	public String logList() {
		logService.build("PAGEVIEW", null, null, "페이지간 이동을 실현한다: 로그/목록 보기", "GET", "/employee/manager/LOG/logList");
		return "Employees/manager/LOG/logList";
	}
	
	// 직원 리스트 페이지  /employee/manager/HRM/hrmEmployeeList
	@GetMapping("/manager/HRM/hrmEmployeeList")
	public String hrmEmployeeList() {
		return "Employees/manager/HRM/hrmEmployeeList";
	}
	// 직원 상세 페이지 직원 pk를 받아서 넘긴다.   /employee/manager/HRM/hrmEmployeeDetail
	@GetMapping("/manager/HRM/hrmEmployeeDetail")
	public String hrmEmployeeDetailList() {
		return "Employees/manager/HRM/hrmEmployeeDetail";
	}
	
	
	
	
	
	// 스테프 페이지,
	@GetMapping("/staff/staffPage")
	public String staffPage() {
		return "/Employees/staff/staffPage";
	}
	
	// 제안서 작성 페이지 이동
	@GetMapping("/staff/writeSuggestionPage")
	public String writeSuggestionPage(
			@AuthenticationPrincipal String username
			) {
		
		if (username == null) {	
			System.out.println("사용자정보가 없는데?");
            return "redirect:/employee/loginPage";
        }
		
		System.out.println("로그인 한 유저 id : " + username);
		
		return "/Employees/staff/writeSuggestionPage";
	}
	
	
	
	
	
	
}
