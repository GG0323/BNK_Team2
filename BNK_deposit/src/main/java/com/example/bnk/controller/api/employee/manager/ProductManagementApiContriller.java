package com.example.bnk.controller.api.employee.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dao.product.IProductDao;
import com.example.bnk.dto.product.ProductDto;

@RestController
@RequestMapping("/api/product")
public class ProductManagementApiContriller {
	
	@Autowired
	private IProductDao productDao;
	
	@GetMapping("/allList")
	public List<ProductDto> allProduct() {
		
		List<ProductDto> dtoList = productDao.showProduct();
		
		return dtoList;
	}
	
	// 상품 판매 현황 테이블과 연계된부분으로 발전가능 상품 판매 관리
	
	
}
