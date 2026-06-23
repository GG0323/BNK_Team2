package com.example.bnk.service.employees.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bnk.dao.product.IProductRateDao;
import com.example.bnk.dto.product.ProductRateDto;

@Service
public class StaffPendingService {

	@Autowired
	IProductRateDao iProductRateDao;
	
	// 금리 등록 서비스!
	public int insertAllRate(ProductRateDto rateDto) {
		int result = 0;
		if(rateDto == null) {
			System.out.println("입력된 금리 DTO가 NULL을 가지고 있습니다.");
			return 0;
		}
		result = iProductRateDao.insertAllRate(rateDto);
		if(result == 0) {
			System.out.println("DB에 입력하던 중 오류가 발생하였습니다.");
			return 0;
		}
		System.out.println("금리 등록이 완료되었습니다.");
		return 1;
	}
	
}
