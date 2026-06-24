package com.example.bnk.controller.api.employee.staff;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dto.product.PendingProductDetailDto;
import com.example.bnk.dto.product.PendingProductListDto;
import com.example.bnk.dto.product.ProductConditionDto;
import com.example.bnk.dto.product.ProductDescriptionDto;
import com.example.bnk.dto.product.ProductRateDto;
import com.example.bnk.dto.product.ProductTermsDto;
import com.example.bnk.service.employees.staff.StaffPendingService;
import com.example.bnk.service.product.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff/product")
@RequiredArgsConstructor
public class StaffProductApiController {	// RestAPI를 사용하는 pendding 기능 컨트롤러 입니다.
	
	
	@Autowired
	ProductService productService;
	@Autowired
	StaffPendingService pendingService;
	
	
	// 모든 pending 상품 리스트 출력
	@GetMapping("/pendingList")
	public List<PendingProductListDto> pendingList(){
		System.out.println("대기 상품 불러오기");
		List<PendingProductListDto> list = productService.pendingList();
		
		return list;
	}
	
	
	// 특정 pending 상품 상세 출력
	@GetMapping("/pendingDetail")
	public PendingProductDetailDto pendingDetail(
			@RequestParam("product_no") Long product_no
			) {
		System.out.println("디테일 불러오기 : " + product_no);
		PendingProductDetailDto dto = productService.pendingDetail(product_no);
		
		return dto;
	}

	
	// 금리 등록
	@PostMapping("/rate/save")
	public Map<String, Object> saveRate(
			@RequestBody ProductRateDto rateDto){
		if(pendingService.insertRate(rateDto) == 0) {
			return Map.of("result", "금리 등록에 실패하였습니다!");
		}
		return Map.of("result", "금리 등록이 완료되었습니다!");
	}
	
	
	//약관 등록
	@PostMapping("/terms/save")
	public Map<String, Object> saveTerms(
			ProductTermsDto termsDto,
			@RequestParam(value = "pdf_file", required = false) MultipartFile pdfFile,
			@RequestParam(value = "image_file", required = false) MultipartFile imageFile
			){
		
		try {
			if(pendingService.insertTerms(termsDto, pdfFile, imageFile) == 0) {
				return Map.of("result", "약관 등록에 실패하였습니다!");
			}			
		}catch(java.io.IOException e) {
			System.out.println("파일 저장 중 하드디크스 에러 발생: " + e.getMessage());
			e.printStackTrace();
		}
		return Map.of("result", "약관 등록이 완료되었습니다!");
	}
	
	
	// 설명 등록
	@PostMapping("/description/save")
	public Map<String, Object> saveDescription(
			ProductDescriptionDto descriptionDto){
		try {
			if(pendingService.insertDescription(descriptionDto) == 0) {
				return Map.of("result", "설명 등록에 실패하였습니다.");
			}			
		}catch(IOException e) {
			System.out.println("설명 등록 중 오류 발생");
			e.printStackTrace();
		}
		return Map.of("result", "설명 등록이 완료되었습니다!");
	}
	
	
	// 조건 등록
	@PostMapping("/condition/save")
	public Map<String, Object> saveCondition(
			@RequestBody ProductConditionDto conditionDto){
		
		System.out.println(conditionDto);
		
		if(pendingService.insertCondition(conditionDto) == 0) {
			System.out.println("조건 등록 중 오류 발생");
		}
		
		return Map.of("result", "조건 등록이 완료되었습니다.");
	}
}
