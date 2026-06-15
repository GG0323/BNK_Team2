package com.example.bnk.controller.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bnk.service.log.MemberPageLogService;

@Controller
@RequestMapping("/employee/manager/LOG/pageLog")
public class EmployeePageLogController {
	
    @Autowired
    private MemberPageLogService logService;
    
    //로그 목록 페이지    /employee/manager/LOG/pageLog/list
    @GetMapping("/list")
    public String logList() {
    	System.out.println("로그 조회 페이지 이동");
        return "Employees/manager/LOG/pageLogList";
    }
	
	// 로그 정리 페이지   /employee/manager/LOG/pageLog/stats
    @GetMapping("/stats")
    public String statsPage() {
    	System.out.println("로그 정리 페이지 이동");
        return "Employees/manager/LOG/pageLogStats";
    }
    
    
}
