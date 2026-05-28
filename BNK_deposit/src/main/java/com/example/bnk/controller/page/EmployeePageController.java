package com.example.bnk.controller.page;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bnk.dto.product.ProductConditionDto;
import com.example.bnk.dto.product.ProductDescriptionDto;
import com.example.bnk.dto.product.ProductDetailResponseDto;
import com.example.bnk.dto.product.ProductDto;
import com.example.bnk.dto.product.ProductRateDto;
import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.product.ProductForEmployee;


@Controller
@RequestMapping("/employee")
public class EmployeePageController {
	
	@Autowired
	EmployeeLogService logService;
	//logService.build("INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.", "POST", "/api/employee/HRM/regist");
	@Autowired
	ProductForEmployee prdForEmpService;

	
	
	// /employee/toMain
	@GetMapping("/toMain") 
	public String mainWorkSpace(Model model,
			@RequestParam(value = "message", required = false)String msg) {
		if(msg != null) {
			model.addAttribute("msg", msg);
		}
		
		return "Employees/mainWorkspaceLogin";
	}
	
	// /Employees/manager/managerPage
	@GetMapping("/manager/managerPage")
	public String managerPage() {
		return "Employees/manager/managerPage";
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
	
	// /employee/manager/HRM/hrmEmployeeDetail
	@GetMapping("/manager/HRM/hrmEmployeeDetail")
	public String hrmEmployeeDetailList() {
		return "Employees/manager/HRM/hrmEmployeeDetail";
	}

	// !! 제안서 리스트 페이지    /employee/manager/SUG/suggestionList
	@GetMapping("/manager/SUG/suggestionList")
	public String suggestionList() {
		return "Employees/manager/SUG/suggestionListPage";
	}
	// !! 제안서 상세 페이지
	@GetMapping("/manager/SUG/suggestionReview")
	public String suggestionReview() {
		return "Employees/manager/SUG/suggestionReviewPage";
	}
	// !! 승인된 제안서 리스트 (중간 테이블)  /employee/manager/SUG/approvedList
	@GetMapping("/manager/SUG/approvedList")
	public String approvedList() {
		return "Employees/manager/SUG/approvedSuggestionList";
	}
	// !! 승인된 제안서 상세 (중간 테이블)
	@GetMapping("/manager/SUG/approvedDetail")
	public String approvedDetail() {
		return "Employees/manager/SUG/approvedSuggestionDetail";
	}
	
	
	

	
	// 스테프 페이지,  /employee/staff/staffPage
	@GetMapping("/staff/staffPage")
	public String staffPage() {
		return "Employees/staff/staffPage";
	}
	// 약관 등록 페이지   /employee/staff/productTerm
	@GetMapping("/staff/productTerm")
	public String productTermForm() {
		return "Employees/staff/term/productTermForm";
	}
	// 제안서 작성 페이지 이동  /employee/staff/writeSuggestionPage
	@GetMapping("/staff/writeSuggestionPage")
	public String writeSuggestionPage(
			@AuthenticationPrincipal String username
			) {
		
		if (username == null) {	
			System.out.println("사용자정보가 없는데?");
            return "redirect:/employee/loginPage";
        }
		
		System.out.println("로그인 한 유저 id : " + username);
		
		return "Employees/staff/writeSuggestionPage";
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
	// /employee/staff/product/condition
	@GetMapping("/staff/product/condition")
	public String goWriteToCondition(Model model) {
		
		List<ApprovedSuggestionDetailDto> approvedSuggestion = prdForEmpService.showAllApprovedSuggestionList();
		model.addAttribute("approvedSuggestion", approvedSuggestion);
		
		return"Employees/staff/productConditionWrite";
	}
	
	// 상품 가입 조건 등록하기
	// employee/staff/product/condition/save
	@PostMapping("/staff/product/condition/save")
	public String saveProductCondition(ProductConditionDto prdCndDto, @RequestParam("suggestion_no") long suggestion_no) {
		
		if(prdForEmpService.insertAllCondition(prdCndDto, suggestion_no) == 1) {
			System.out.println("1. condition 등록하기 성공");
			return "redirect:/employee/staff/product/list";
		}
		
		System.out.println("1. condition 등록하기 실패");
		return "redirect:/employee/staff/product/condition";
	}
	
	// 상품 금리 등록 페이지
	// /employee/staff/product/rate
	@GetMapping("/staff/product/rate")
	public String goWriteToRate(Model model) {
		
		List<ApprovedSuggestionDetailDto> approvedSuggestion = prdForEmpService.showAllApprovedSuggestionList();
		model.addAttribute("approvedSuggestion", approvedSuggestion);
		
		return "Employees/staff/productRateWrite";
	}
	
	// 상품 금리 등록하기
	// employee/staff/product/rate/save
	@PostMapping("/staff/product/rate/save")
	public String saveProductRate(ProductRateDto prdRateDto,
			@RequestParam("suggestion_no") long suggestion_no) {
		
//		System.out.println(prdRateDto);
		
		if(prdForEmpService.insertAllRate(prdRateDto, suggestion_no) == 1) {
			System.out.println("1. rate_no 등록하기 성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("1. rate_no 등록하기 실패");
		return "redirect:/employee/staff/product/descrition";
	}
	
	// 상품 설명 관리 페이지
	// /employee/staff/product/description
	@GetMapping("/staff/product/description")
	public String goWriteToDescription(Model model) {
		
		List<ApprovedSuggestionDetailDto> approvedSuggestion = prdForEmpService.showAllApprovedSuggestionList();
		model.addAttribute("approvedSuggestion", approvedSuggestion);
		
		return "Employees/staff/productDescriptionWrite";
	}
	
	// 상품 설명 관리 등록하기
	@PostMapping("/staff/product/description/save")
	public String saveProductDescription(ProductDescriptionDto prdDescDto,
			 @RequestParam("suggestion_no") long suggestion_no) throws IOException {
		
		if(prdForEmpService.saveDescription(prdDescDto, suggestion_no) == 1) {
			System.out.println("1. descriptoin_no 등록하기 성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("1. description_no 등록하기 실패");
		
		return "redirect:/employee/staff/product/description";
	}
	
	
	/* 상품 상세 페이지 수정용.
	-----------------------------------------------------------------------------------------*/
	// 상품 기본 정보 수정
	@PostMapping("staff/product/update/product")
	public String updateProduct(ProductDto productDto) {
		int product_no = (int) productDto.getProduct_no();
		
		if(prdForEmpService.updateProductStatus(productDto) == 1) {
			System.out.println("성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("실패");
		
		return "redirect:/employee/prdPage/detail/" + product_no;
	}
	
	// 상품 금리 정보 수정
	@PostMapping("staff/product/update/rate")
	public String updateRate(ProductRateDto productRateDto){
		int product_no = (int) productRateDto.getProduct_no();
		
		if(prdForEmpService.updateRateStatus(productRateDto) == 1) {
			System.out.println("성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("실패");
		return "redirect:/employee/prdPage/detail" + product_no;
	}
	
	// 상품 설명 변경
	@PostMapping("/staff/product/update/description")
	public String updateDescription(ProductDescriptionDto prdDescDto) {
		int product_no = (int) prdDescDto.getProduct_no();
		
		if(prdForEmpService.updateProductDescription(prdDescDto) == 1) {
			System.out.println("성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("실패");
		return "redirect:/employee/prdPage/detail" + product_no;
	}
	
	// 상품 가입 조건 변경
	@PostMapping("/staff/product/update/condition")
	public String updateProductCondition(ProductConditionDto prdCndDto) {
		int product_no = (int) prdCndDto.getProduct_no();
		
		if(prdForEmpService.updateProductCondition(prdCndDto) == 1) {
			System.out.println("성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("실패");
		return "redirect:/employee/prdPage/detail" + product_no;
	}
}
