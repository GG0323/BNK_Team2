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
import com.example.bnk.dto.product.ProductDto;
import com.example.bnk.service.product.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff/product")
@RequiredArgsConstructor
public class StaffProductApiController {

	@Autowired
	ProductService serv;
	
	
	@PostMapping("/insertProduct")
	public Map<String, Long> insertProduct(
			@RequestBody ProductDto product
			) {
		System.out.println("상품 등록 파라미터 :"+product.toString());
		Long productPk = serv.insertProduct(product);
		
		return Map.of("product_no", productPk);
	}
	
	@GetMapping("/pendingList")
	public List<PendingProductListDto> pendingList(){
		System.out.println("대기 상품 불러오기");
		List<PendingProductListDto> list = serv.pendingList();
		
		return list;
	}
	
	@GetMapping("/pendingDetail")
	public PendingProductDetailDto pendingDetail(
			@RequestParam("product_no") Long product_no
			) {
		System.out.println("디테일 불러오기 : " + product_no);
		PendingProductDetailDto dto = serv.pendingDetail(product_no);
		
		return dto;
	}
	
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
}
