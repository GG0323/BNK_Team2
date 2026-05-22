package com.example.bnk.controller.page;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bnk.dao.employee.IEmployeeDao;
import com.example.bnk.dao.member.IBankMemberDao;
import com.example.bnk.dao.product.IProductDao;
import com.example.bnk.dao.product.IProductSuggestionDao;
import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.product.ProductDto;
import com.example.bnk.dto.product.ProductSuggestionDto;


@Controller
public class ManagerPageController {
	
	@Autowired
	private IProductSuggestionDao ppsService;
	
	@Autowired
	private IProductDao prdService;
	
	@Autowired
	private IEmployeeDao empService;
	
	@Autowired
	private IBankMemberDao bnkmemService;
	
	/* 관리자 페이지
	------------------------------------------------------------------------------------*/
	
	// 관리자 메인페이지
	@GetMapping("/mngMain")
	public String goManagerMainPage() {
		return "manager/managerMain";
	}
	
	// 관리자 - 제안서 관리 페이지 이동
	@GetMapping("/ppsPage")
	public String goProposalPage(Model model) {
		
	    List<ProductSuggestionDto> ppsList = ppsService.showPrdSugt(); 
	    
	    // NULL 방지 -> 새로운 배열을 만들어서 넘기며 오류발생 막기
	    if (ppsList == null) {
	        ppsList = new ArrayList<>();
	    }
	    
	    model.addAttribute("ppsList", ppsList);
	    
	    return "manager/proposal";
	}
	
	// 관리자 - 상품 관리 페이지 이동
	@GetMapping("/prdPage")
	public String goProductPage(Model model) {
		List<ProductDto> prdList = prdService.showProduct();
		
		// NULL 방지
		if(prdList == null) {
			prdList = new ArrayList<>();
		}
		
		model.addAttribute("prdList", prdList);
		
		return "manager/product";
	}
	
	// 관리자 - 직원 관리 페이지 이동
	@GetMapping("/stfPage")
	public String goManagerPage(Model model) {
		List<EmployeeDto> empList = empService.showAllEmp();
		
		// 방지
		if(empList == null) {
			empList = new ArrayList<>();
		}
		model.addAttribute("empList", empList);
		
		return "manager/staff";
	}
	
	// 관리자 - 회원 관리 페이지 이동
	@GetMapping("/mmbPage")
	public String goMemberPage() {
		return "manager/member";
	}
	
	// 관리자 - 회원 관리 페이지 검색 기능 (타임리프 fragment 방식)
	@GetMapping("/mmbPage/search")
	public String handleSearchRequest(
	        @RequestParam(value = "birth_date", required = false) String birth_date,
	        @RequestParam(value = "phone_number", required = false) String phone_number,
	        @RequestParam(value = "member_name", required = false) String member_name,
	        Model model) {
	    
	    List<BankMemberDto> resultList = bnkmemService.showMember(birth_date, phone_number, member_name);
	    
	    if (resultList == null) {
	        resultList = new ArrayList<>();
	    }
	    
	    model.addAttribute("mmbList", resultList);
	    
	    return "manager/member :: #memberTableResult"; 
	}
	
	// 관리자 - 커뮤니티 공지 작성 페이지
	@GetMapping("/comuPage")
	public String writeComuPage() {
		return "manager/comu";
	}
	
	
}
