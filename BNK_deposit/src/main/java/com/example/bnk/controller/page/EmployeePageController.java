package com.example.bnk.controller.page;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dto.product.ProductConditionDto;
import com.example.bnk.dto.product.ProductDescriptionDto;
import com.example.bnk.dto.product.ProductDto;
import com.example.bnk.dto.product.ProductRateDto;
import com.example.bnk.dto.product.ProductTermsDto;
import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.product.ProductForEmployee;

@Controller
@RequestMapping("/employee")
public class EmployeePageController {

	@Autowired
	EmployeeLogService logService;

	@Autowired
	ProductForEmployee prdForEmpService;

	@Value("${file.upload.path}")
    private String uploadPath;
	
	@Value("${file.upload.url}")
    private String uploadUrl;
	
	// /employee/toMain
	@GetMapping("/toMain")
	public String mainWorkSpace(Model model, @RequestParam(value = "message", required = false) String msg) {
		if (msg != null) {
			model.addAttribute("msg", msg);
		}

		return "Employees/mainWorkspaceLogin";
	}

	// /Employees/manager/managerPage
	@GetMapping("/manager/managerPage")
	public String managerPage(@AuthenticationPrincipal String username) {
		logService.build(username, "INSERT", "TB_EMPLOYEE", null, "신규 사원 등록 요청을 처리한다.");

		return "Employees/manager/managerPage";
	}

	// /employee/manager/HRM/hrmRegist
	@GetMapping("/manager/HRM/hrmRegist")
	public String hrmRegist(@AuthenticationPrincipal String username) {
		logService.build(username, "PAGEVIEW", null, null, "페이지간 이동을 실현한다: 인사관리/신규 사원 등록");
		return "Employees/manager/HRM/hrmRegist";
	}

	// 직원 로그 조회 페이지 /employee/manager/LOG/logList
	@GetMapping("/manager/LOG/logList")
	public String logList(@AuthenticationPrincipal String username) {
		logService.build(username, "PAGEVIEW", null, null, "페이지간 이동을 실현한다: 로그/목록 보기");
		return "Employees/manager/LOG/logList";
	}

	// 직원 리스트 페이지
	// /employee/manager/HRM/hrmEmployeeList
	@GetMapping("/manager/HRM/hrmEmployeeList")
	public String hrmEmployeeList(@AuthenticationPrincipal String username) {
		logService.build(username, "PAGEVIEW", "TB_EMPLOYEE", null, "직원 목록을 불러온다.");
		return "Employees/manager/HRM/hrmEmployeeList";
	}

	// /employee/manager/HRM/hrmEmployeeDetail
	@GetMapping("/manager/HRM/hrmEmployeeDetail")
	public String hrmEmployeeDetailList() {
		return "Employees/manager/HRM/hrmEmployeeDetail";
	}

	// !! 제안서 리스트 페이지 /employee/manager/SUG/suggestionList
	@GetMapping("/manager/SUG/suggestionList")
	public String suggestionList(@AuthenticationPrincipal String username) {
		logService.build(username, "PAGEVIEW", "TB_PRODUCTS_SUGGESTION", null, " 제안서 목록을 불러온다.");
		return "Employees/manager/SUG/suggestionListPage";
	}

	// !! 제안서 상세 페이지
	@GetMapping("/manager/SUG/suggestionReview")
	public String suggestionReview(@AuthenticationPrincipal String username) {
		return "Employees/manager/SUG/suggestionReviewPage";
	}

	// !! 승인된 제안서 리스트 (중간 테이블) /employee/manager/SUG/approvedList
	@GetMapping("/manager/SUG/approvedList")
	public String approvedList(@AuthenticationPrincipal String username) {
		logService.build(username, "PAGEVIEW", "TB_APPROVED_SUGGESTION", null, "승인한 제안서 목록을 불러온다.");
		return "Employees/manager/SUG/approvedSuggestionList";
	}

	// !! 승인된 제안서 상세 (중간 테이블)
	@GetMapping("/manager/SUG/approvedDetail")
	public String approvedDetail(@AuthenticationPrincipal String username) {
		return "Employees/manager/SUG/approvedSuggestionDetail";
	}
	
	
	// 매니저 상세 페이지 -> 상품 디테일의 디테일
	// 브라우저 호출 주소:
	// /manager/SUG/approvedDetail/productDetail?type=rate&suggestion_no=5
	@GetMapping("/manager/SUG/approvedDetail/productDetail")
	public String goProductComponentDetail(@RequestParam("type") String type,
			@RequestParam("suggestion_no") long suggestion_no, Model model) {

		System.out.println("넘어온 컴포넌트 타입: " + type);
		System.out.println("넘어온 제안서 번호: " + suggestion_no);

		model.addAttribute("suggestion_no", suggestion_no);

		ApprovedSuggestionDetailDto aprDto = prdForEmpService.selectApprovedSug(suggestion_no);

		if ("rate".equals(type)) {
			// 금리 등록/상세 페이지로 이동
			ProductRateDto rateDto = prdForEmpService.selectRatePrd(aprDto.getRate_no());
			model.addAttribute("rateDto", rateDto);

			return "Employees/manager/SUG/composition/productRateDetail";
		} else if ("terms".equals(type)) {
			// 약관 등록/상세 페이지로 이동

			return "Employees/manager/SUG/composition/productTermsDetail";
		} else if ("description".equals(type)) {
			// 설명 등록/상세 페이지로 이동
			ProductDescriptionDto desDto = prdForEmpService.selectDescriptionPrd(aprDto.getDescription_no());
			model.addAttribute("desDto", desDto);

			return "Employees/manager/SUG/composition/productDescriptionDetail";
		} else if ("condition".equals(type)) {
			// 조건 등록/상세 페이지로 이동
			ProductConditionDto conDto = prdForEmpService.selectConditionPrd(aprDto.getCondition_no());
			model.addAttribute("conDto", conDto);

			return "Employees/manager/SUG/composition/productConditionDetail";
		}

		// 알 수 없는 타입인 경우 에러 페이지나 목록으로 리다이렉트
		System.out.println("뭔가 잘못된 클릭입니다.");
		return "redirect:/employee/manager/SUG/approvedList";
	}

	// 고객 추적 LOG
	@GetMapping("/manager/LOG/bankMemberLog")
	public String bankMemberLog() {
		return "Employees/manager/LOG/bankMemberLogList";
	}

	// 상품 리스트 /employee/manager/PRD/productList
	@GetMapping("/manager/PRD/productList")
	public String productList() {
		return "Employees/manager/PRD/productList";
	}

	// 스테프 페이지, /employee/staff/staffPage
	@GetMapping("/staff/staffPage")
	public String staffPage() {
		return "Employees/staff/staffPage";
	}

	// 약관 등록 페이지 /employee/staff/productTerm --------------------------------------------------------------------------------
	@GetMapping("/staff/productTerm")
	public String productTermForm() {
		return "Employees/staff/term/productTermForm";
	}

	// 제안서 작성 페이지 이동 /employee/staff/writeSuggestionPage
	@GetMapping("/staff/writeSuggestionPage")
	public String writeSuggestionPage(@AuthenticationPrincipal String username) {

		if (username == null) {
			System.out.println("사용자정보가 없는데?");
			return "redirect:/employee/loginPage";
		}

		System.out.println("로그인 한 유저 id : " + username);

		return "Employees/staff/writeSuggestionPage";
	}

	/*
	 * 사원 페이지에서 상품 리스트 출력 및 관리 용도 코드
	 * -----------------------------------------------------------------------------*/
	// /employee/staff/product/list
	@GetMapping("/staff/product/list")
	public String employeeProductListPage(Model model) {
		
		List<ProductDto> productList = prdForEmpService.showProduct();
		
		model.addAttribute("productList", productList);

		return "Employees/staff/productList";
	}

	// 상품 디테일 페이지.
	// URL 경로는 기존 그대로 유지하되, 내부 로직을 보강합니다.
	@GetMapping("/staff/product/approved/details/{suggestion_no}")
	public String goProductDetailPage(@PathVariable("suggestion_no") String suggestion_no, Model model) {

		ApprovedSuggestionDetailDto aprSugDto = prdForEmpService.selectApprovedSug(Integer.parseInt(suggestion_no));

		model.addAttribute("aprSugDto", aprSugDto);

		return "Employees/staff/productDetails";
	}

	/*
	 * ----------------------------------------------------------------------------- */
	// 승인된 상품 상세 페이지의 상세 사양 페이지
	@GetMapping("/staff/product/approved/{type}/detail")
	public String approvedProductDetail(@PathVariable("type") String type,
			@RequestParam("suggestion_no") String suggestion_no, @RequestParam("no") String no, Model model) {

		switch (type) {
		case "rate":
			ProductRateDto rateDto = prdForEmpService.selectRatePrd(Integer.parseInt(no));
			model.addAttribute("rateDto", rateDto);
			return "/Employees/staff/composition/productRateDetail";
		case "term":
			ProductTermsDto termsDto = prdForEmpService.selectTermsPrd(Integer.parseInt(no));
			model.addAttribute("termsDto", termsDto);
			return "/Employees/staff/composition/productTermsDetail";
		case "description":
			ProductDescriptionDto desDto = prdForEmpService.selectDescriptionPrd(Integer.parseInt(no));
			model.addAttribute("desDto", desDto);
			return "/Employees/staff/composition/productDescriptionDetail";
		case "condition":
			ProductConditionDto conDto = prdForEmpService.selectConditionPrd(Integer.parseInt(no));
			model.addAttribute("conDto", conDto);
			return "/Employees/staff/composition/productConditionDetail";
		default:
			break;

		}

		return "redirect:/employee/staff/approved/list/detail?" + suggestion_no;
	}
	
	/* 등록
	-----------------------------------------------------------------------------------------*/

	// 상품 가입 조건 등록 페이지
	// /employee/staff/product/condition
	@GetMapping("/staff/product/condition")
	public String goWriteToCondition(Model model) {

		List<ApprovedSuggestionDetailDto> approvedSuggestion = prdForEmpService.showAllApprovedSuggestionList();
		model.addAttribute("approvedSuggestion", approvedSuggestion);

		return "Employees/staff/productConditionWrite";
	}

	// 상품 가입 조건 등록하기
	// employee/staff/product/condition/save
	@PostMapping("/staff/product/condition/save")
	public String saveProductCondition(ProductConditionDto prdCndDto,
			@RequestParam("suggestion_no") long suggestion_no) {

		if (prdForEmpService.insertAllCondition(prdCndDto, suggestion_no) == 1) {
			System.out.println("1. condition 등록하기 성공");
			return "redirect:/employee/staff/product/approved/list";
		}

		System.out.println("1. condition 등록하기 실패");
		return "redirect:/employee/staff/product/condition";
	}

	// 상품 금리 등록 페이지
	// /employee/staff/product/rate
	@GetMapping("/staff/product/rate")
	public String goWriteToRate(Model model,
			@RequestParam("suggestion_no") long suggestion_no) {
		
		model.addAttribute("suggestion_no", suggestion_no);
		ProductRateDto dto = prdForEmpService.selectRatePrd(suggestion_no);
		model.addAttribute("rate", dto);
		
		
		ApprovedSuggestionDetailDto approvedSug = prdForEmpService.selectApprovedSug(suggestion_no);
		model.addAttribute("approvedSug", approvedSug);
		
		
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

		if (prdForEmpService.insertAllRate(prdRateDto, suggestion_no) == 1) {
			System.out.println("1. rate_no 등록하기 성공");
			return "redirect:/employee/staff/product/approved/list";
		}
		System.out.println("1. rate_no 등록하기 실패");
		return "redirect:/employee/staff/product/descrition";
	}

	// 약관 등록 페이지 이동
	// employee/staff/product/terms
	@GetMapping("/staff/product/terms")
	public String goWriteToTerms(Model model) {
		
		List<ApprovedSuggestionDetailDto> approvedSuggestion = prdForEmpService.showAllApprovedSuggestionList();
		model.addAttribute("approvedSuggestion", approvedSuggestion);
		
		return "Employees/staff/productTermsWrite";
	}
	
	
	// 상품 약관 등록하기
	// employee/staff/product/terms/save
	@PostMapping("/staff/product/terms/save")
	public String saveProductTerms(
			ProductTermsDto prdTermsDto,
			@RequestParam("suggestion_no") long suggestion_no,
			@RequestParam(value = "pdf_file", required = false) MultipartFile pdfFile,
			@RequestParam(value = "image_file", required = false) MultipartFile imageFile) {
		
		System.out.println("====== [컨트롤러] 약관 등록 프로세스 시동 ======");
		System.out.println("기본 텍스트 데이터: " + prdTermsDto);
		System.out.println("타겟 상품 제안서 번호: " + suggestion_no);
		System.out.println("수신된 PDF 파일 존재 여부: " + (pdfFile != null && !pdfFile.isEmpty()));
		System.out.println("수신된 이미지 파일 존재 여부: " + (imageFile != null && !imageFile.isEmpty()));
		
		try {
			int result = prdForEmpService.insertAllTerms(prdTermsDto, suggestion_no, pdfFile, imageFile);
			
			if (result == 1) {
				System.out.println("하드디스크 파일 저장 및 DB 등록 최종 성공!");
				return "redirect:/employee/staff/product/approved/list";
			}
			
		} catch (java.io.IOException e) {
			System.out.println("파일 물리 저장 중 하드디스크 에러 발생: " + e.getMessage());
			e.printStackTrace();
		}
		
		return "redirect:/employee/staff/product/terms";
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

		if (prdForEmpService.saveDescription(prdDescDto, suggestion_no) == 1) {
			System.out.println("1. descriptoin_no 등록하기 성공");
			return "redirect:/employee/staff/product/approved/list";
		}
		System.out.println("1. description_no 등록하기 실패");

		return "redirect:/employee/staff/product/description";
	}

	// 승인된 상품 리스트 출력 페이지
	@GetMapping("/staff/product/approved/list")
	public String showAllApprovedList(Model model) {

		List<ApprovedSuggestionDetailDto> approvedSuggestion = prdForEmpService.showAllApprovedSuggestionList();
		model.addAttribute("approvedSuggestion", approvedSuggestion);
		System.out.println(approvedSuggestion);
		return "Employees/staff/ApprovedProductList";
	}

	/* 상품 상세 페이지 수정용.
	----------------------------------------------------------------------------- */
	// 상품 기본 정보 수정
	@PostMapping("staff/product/update/product")
	public String updateProduct(ProductDto productDto) {
		int product_no = (int) productDto.getProduct_no();

		if (prdForEmpService.updateProductStatus(productDto) == 1) {
			System.out.println("성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("실패");

		return "redirect:/employee/prdPage/detail/" + product_no;
	}

	// 상품 금리 정보 수정
	@PostMapping("staff/product/update/rate")
	public String updateRate(ProductRateDto productRateDto) {
		int product_no = (int) productRateDto.getProduct_no();

		if (prdForEmpService.updateRateStatus(productRateDto) == 1) {
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

		if (prdForEmpService.updateProductDescription(prdDescDto) == 1) {
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

		if (prdForEmpService.updateProductCondition(prdCndDto) == 1) {
			System.out.println("성공");
			return "redirect:/employee/staff/product/list";
		}
		System.out.println("실패");
		return "redirect:/employee/prdPage/detail" + product_no;
	}
	
	// /employee/staff/reservation
    @GetMapping("/staff/reservation")
    public String staffReservationPage(@AuthenticationPrincipal String username) {
        logService.build(username, "PAGEVIEW", "TB_RESERVATION", null, "예약 관리 페이지 접근");
        return "Employees/staff/reservation";
    }
    
    
    /** 수정된 상품 등록 로직 */  //  /employee/manager/writeProduct    /employee/manager/pendingProductDetail
    @GetMapping("/manager/writeProduct")
    public String writeProduct() {
    	return "Employees/manager/writeProductTemp";
    }
    @GetMapping("/manager/pendingProductList")
    public String pendingProductList() {
    	return "Employees/manager/pendingProductList";
    }
    @GetMapping("/manager/pendingProductDetail")
    public String pendingProductDetail() {
    	return "Employees/manager/pendingProductDetail";
    }
    
    
    
    
    
    
    
    
    
    
    
    
}
