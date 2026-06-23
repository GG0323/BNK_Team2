package com.example.bnk.controller.api.employee.staff;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.product.PendingProductDetailDto;
import com.example.bnk.dto.product.PendingProductListDto;
import com.example.bnk.dto.product.ProductRateDto;
import com.example.bnk.dto.product.ProductTermsDto;
import com.example.bnk.service.employees.staff.StaffPendingService;
import com.example.bnk.service.product.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff/product")
@RequiredArgsConstructor
public class StaffProductApiController {

	@Autowired
	ProductService serv;
	
	@Autowired
	StaffPendingService pendingService;
	
	
	// 모든 pending 상품 리스트 출력
	@GetMapping("/pendingList")
	public List<PendingProductListDto> pendingList(){
		System.out.println("대기 상품 불러오기");
		List<PendingProductListDto> list = serv.pendingList();
		
		return list;
	}
	
	
	// 특정 pending 상품 상세 출력
	@GetMapping("/pendingDetail")
	public PendingProductDetailDto pendingDetail(
			@RequestParam("product_no") Long product_no
			) {
		System.out.println("디테일 불러오기 : " + product_no);
		PendingProductDetailDto dto = serv.pendingDetail(product_no);
		
		return dto;
	}
	
	
	// 관리자용 승인요청인데 이거는 음..... 직원이 요청하기로 바꿀지, 아니면 그냥 없앨지 고민중
	@PostMapping("/approvePending")
	@Transactional
	public Map<String, Object> approvePending(
			@RequestBody Map<String, Object> body
			) {
	    Long product_no = Long.parseLong(body.get("product_no").toString());
	    System.out.println("승인 요청 product_no : " + product_no);

	    int updated = serv.approvePending(product_no);   // 서비스가 0 또는 1 반환
	    return Map.of("result", updated);
	}
	
	
	// 금리 등록
	@PostMapping("/rate/save")
	public Map<String, Object> saveRate(
			@RequestBody ProductRateDto rateDto){
		if(pendingService.insertAllRate(rateDto) == 0) {
			return Map.of("result", "금리 등록에 실패하였습니다!");
		}
		return Map.of("result", "금리 등록이 완료되었습니다!");
	}
	
	//약관 등록
	@PostMapping("/terms/save")
	public Map<String, Object> saveTerms(
			@RequestBody ProductTermsDto termsDto){
		if(pendingService.insertAllTerms(termsDto) == 0) {
			return Map.of("result", "약관 등록에 실패하였습니다!");
		}
		return Map.of("result", "약관 등록이 완료되었습니다!");
	}
	
}
