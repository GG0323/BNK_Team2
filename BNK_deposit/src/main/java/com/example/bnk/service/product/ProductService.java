package com.example.bnk.service.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.product.IProductDao;
import com.example.bnk.dto.product.PendingProductDetailDto;
import com.example.bnk.dto.product.PendingProductListDto;
import com.example.bnk.dto.product.ProductDto;

@Service
public class ProductService {

	@Autowired
	IProductDao dao;
	
	
	
	
	public Long insertProduct(ProductDto product) {
		dao.insertProduct(product); // keyProperty 로 자동 바인딩
		System.out.println("등록된 상품 PK : "+product.getProduct_no());
		return product.getProduct_no();
	}

	public List<PendingProductListDto> pendingList() {
		List<PendingProductListDto> list = dao.selectPendingProductList();
		System.out.println("대기 상품 불러오기 : " + list);
		return list;
	}

	public PendingProductDetailDto pendingDetail(Long product_no) {
		PendingProductDetailDto dto = dao.selectPendingProductDetail(product_no);
		System.out.println("대기 상품 상세 : " + dto);
		return dto;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
