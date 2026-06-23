package com.example.bnk.service.employees.staff;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dao.product.IProductRateDao;
import com.example.bnk.dao.product.IProductTermsDao;
import com.example.bnk.dto.product.ProductRateDto;
import com.example.bnk.dto.product.ProductTermsDto;

@Service
public class StaffPendingService {

	@Autowired
	IProductRateDao iProductRateDao;
	@Autowired
	IProductTermsDao iProductTermsDao;
	
	// 이미지 저장용
	@Value("${file.upload.path}")
    private String uploadPath; // C:/upload/
    @Value("${file.upload.url}")
    private String uploadUrl;  // /upload/
	
	
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
	
	// 약관 등록 및 파일 저장 서비스
	@Transactional
	public int insertAllTerms(
			ProductTermsDto termsDto,
			MultipartFile pdfFile,
			MultipartFile imageFile) throws IOException{
		
		File uploadFolder = new File(uploadPath);
		if(!uploadFolder.exists()) {
			uploadFolder.mkdirs();
		}
		
		if(pdfFile != null && !pdfFile.isEmpty()) {
			String originalName = pdfFile.getOriginalFilename();
			String savedName = UUID.randomUUID().toString() + "_" + originalName;
			
			pdfFile.transferTo(new File(uploadPath + savedName));
			
			termsDto.setPdf_url(uploadUrl + savedName);
		}
		
		int result = 0;
		if(termsDto.getProduct_no() == 0) {				// product_no가 0이라면 나머지도 안들어왔다는 거.
			System.out.println("입력된 약관 DTO가 NULL을 가지고 있습니다");
			return 0;
		}
		
		result = iProductTermsDao.insertProductTerms(termsDto);
		if(result == 0) {
			System.out.println("DB에 입력하던 중 오류가 발생하였습니다.");
			return 0;
		}
		System.out.println("약관 등록이 완료되었습니다.");
		return 1;
	}
	
}


