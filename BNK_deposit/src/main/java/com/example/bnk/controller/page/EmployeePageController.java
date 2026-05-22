package com.example.bnk.controller.page;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bnk.dto.product.ProductConditionDto;
import com.example.bnk.dto.product.ProductDescriptionDto;
import com.example.bnk.dto.product.ProductDetailResponseDto;
import com.example.bnk.dto.product.ProductDto;
import com.example.bnk.dto.product.ProductRateDto;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.product.ProductForEmployee;

@Controller
@RequestMapping("/employee")
public class EmployeePageController {
	
	@Autowired
	EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	
	@Autowired
	private ProductForEmployee prdForEmpService;
	
	
	// /employee/toMain
	@GetMapping("/toMain") 
	public String mainWorkSpace() {
		return "Employees/mainWorkspaceLogin";
	}
	
	// /employee/manager/HRM/hrmRegist
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
	
	// 직원 리스트 페이지 
	// /employee/manager/HRM/hrmEmployeeList
	@GetMapping("/manager/HRM/hrmEmployeeList")
	public String hrmEmployeeList() {
		return "Employees/manager/HRM/hrmEmployeeList";
	}
	
	// /employee/manager/HRM/hrmEmployeeDetailList
	@GetMapping("/manager/HRM/hrmEmployeeDetailList")
	public String hrmEmployeeDetailList() {
		return "Employees/manager/HRM/hrmEmployeeDetailList";
	}
	
	/* 사원 페이지에서 상품 리스트 출력 및 관리 용도 코드 
	-------------------------------------------------------------------------------------*/
	// /employee/staff/product/list
	@GetMapping("/staff/product/list")
	public String employeeProductListPage(Model model) {
		// 상품 리스트를 productList에 저장
		List<ProductDto> productList = prdForEmpService.showProduct();
		// productList를 model에 저장
		model.addAttribute("productList", productList);
		
		return "Employees/staff/productList";
	}
	
	
	// 상품 디테일 페이지.
	// /employee/prdPage/details/{product_no}
	@GetMapping("/prdPage/detail/{product_no}")
	public String goProductDetailPage(
			@PathVariable("product_no") String product_no,
			Model model) {
	    // 상품 번호로 DB에서 상세 정보 조회
//	    ProductDto product = prdForEmpService.showProductDetails(Integer.parseInt(product_no));
//	    model.addAttribute("product", product_no);
	    
	    // 상품 번호로 DB에서 조인문을 사용한 상세 및 전체 정보 조회
	    ProductDetailResponseDto prdDtResDto = prdForEmpService.selectProductDetail(Long.parseLong(product_no));
	    model.addAttribute("prdDtResDto", prdDtResDto);
	    
	    return "Employees/staff/productDetails";
	}
	
	// 상품 가입 조건 등록 페이지
	// /employee/staff/product/write
	@GetMapping("/staff/product/condition")
	public String goWriteToCondition() {
		
		return"Employees/staff/productConditionWrite";
	}
	
	// 상품 가입 조건 등록하기
	// employee/staff/product/condition/save
	@PostMapping("/staff/product/condition/save")
	public String saveProductCondition(ProductConditionDto prdCndDto) {

//		System.out.println(prdCndDto);
		
		
		if(prdForEmpService.insertAllCondition(prdCndDto) == 1) {
			System.out.println("성공");
		}else {
			System.out.println("실패");
		}
		
		
		return "redirect:/employee/staff/product/list";
	}
	
	// 상품 금리 등록 페이지
	// /employee/staff/product/rate
	@GetMapping("/staff/product/rate")
	public String goWriteToRate() {
		return "Employees/staff/productRateWrite";
	}
	
	// 상품 금리 등록하기
	// employee/staff/product/rate/save
	@PostMapping("/staff/product/rate/save")
	public String saveProductRate(ProductRateDto prdRateDto) {
		
//		System.out.println(prdRateDto);
		
		if(prdForEmpService.insertAllRate(prdRateDto) == 1) {
			System.out.println("성공");
		}else {
			System.out.println("실패");
		}
		return "redirect:/employee/staff/product/list";
	}
	
	// 상품 설명 관리 페이지
	// /employee/staff/product/description
	@GetMapping("/staff/product/description")
	public String goWriteToDescription() {
		return "Employees/staff/productDescriptionWrite";
	}
	
	// 상품 설명 관리 등록하기
	@PostMapping("/staff/product/description/save")
	public String saveProductDescription(ProductDescriptionDto prdDescDto) throws IOException {
		
		System.out.println(prdDescDto);
		
		if(prdForEmpService.saveDescription(prdDescDto) == 1) {
			System.out.println("성공");
		}else {
			System.out.println("실패");
		}
		return "redirect:/employee/staff/product/description";
	}
	
}
