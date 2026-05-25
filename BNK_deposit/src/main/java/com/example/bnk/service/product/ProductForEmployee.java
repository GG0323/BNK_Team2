package com.example.bnk.service.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dao.product.IProductConditionDao;
import com.example.bnk.dao.product.IProductDao;
import com.example.bnk.dao.product.IProductDescriptionDao;
import com.example.bnk.dao.product.IProductDetailResponseDao;
import com.example.bnk.dao.product.IProductRateDao;
import com.example.bnk.dto.product.ProductConditionDto;
import com.example.bnk.dto.product.ProductDescriptionDto;
import com.example.bnk.dto.product.ProductDetailResponseDto;
import com.example.bnk.dto.product.ProductDto;
import com.example.bnk.dto.product.ProductRateDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductForEmployee {
	
	private final IProductDao productDao;
	private final IProductConditionDao iProductionDao;
	private final IProductRateDao iProductRateDao;
	private final IProductDescriptionDao iProductDescDao;
	private final IProductDetailResponseDao iProductDetailResponseDao;
	
	// 이미지 저장용
	@Value("${file.upload.path}")
    private String uploadPath; // C:/upload/
    @Value("${file.upload.url}")
    private String uploadUrl;  // /upload/
	
	
	// 상품 목록 조회
	public List<ProductDto> showProduct(){
		return productDao.showProduct();
	}
	
	// 상품 상세 조회
	public ProductDto showProductDetails(@Param("produt_no")int Product_no) {
		return productDao.showProductDetails(Product_no);
	}
	
	// 상품 JOIN 조회용 서비스
	public ProductDetailResponseDto selectProductDetail(Long Product_no) {
		return iProductDetailResponseDao.selectProductDetail(Product_no);
	}
	
	// 상품 조건 등록 서비스!
	public int insertAllCondition(ProductConditionDto prdCndDto) {
		if(prdCndDto != null) {
			if("ALL".equals(prdCndDto.getGender())) {
				prdCndDto.setGender(null);
			}
			
			if(iProductionDao.insertAllCondition(prdCndDto) == 1) {
				return 1;
			}else {
				System.out.println("DB를 넣는 과정 중 오류가 발생했습니다.");
				return 0;
			}
		}
		
		return 0;
	}
	
	// 상품 금리 등록 서비스1
	public int insertAllRate(ProductRateDto prdRateDto) {
		if(prdRateDto != null) {
			if(iProductRateDao.insertAllRate(prdRateDto) == 1) {
				return 1;
			}else {
				System.out.println("DB를 넣는 과정 중 오류가 발생했습니다.");
				return 0;
			}
		}
		
		return 0;
	}
	
	// 상품 설명 등록 서비스!
	public int insertAllDescription(ProductDescriptionDto prdDescDto) {
		if(prdDescDto != null) {
			if(iProductDescDao.insertAllDescription(prdDescDto) == 1) {
				return 1;
			}else {
				System.out.println("DB를 넣는 과정 중 오류가 발생했습니다.");
				return 0;
			}
			
		}
		return 0;
	}
	
	


    @Transactional
    public int saveDescription(ProductDescriptionDto prdDescDto) throws IOException {
        
        MultipartFile file = prdDescDto.getImage_file();
        
        if (file != null && !file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            
            // 1. 진짜 물리적 경로(uploadPath)에 파일 저장
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            File destination = new File(folder, savedFileName);
            file.transferTo(destination);
            
            // 2. DB에는 웹에서 읽어갈 수 있는 주소 경로(uploadUrl)를 조합해서 저장!
            // 예: "/upload/" + "uuid_file.png" => /upload/uuid_file.png
            prdDescDto.setImage_url(uploadUrl + savedFileName);
            System.out.println(uploadUrl + savedFileName);
        }
        
        if(iProductDescDao.insertAllDescription(prdDescDto) == 1) {
        	return 1;
        }
        
        return 0;
    }
    
    
    // 상품 기본정보 업데이트
    public int updateProductStatus(ProductDto productDto) {
    	if(productDto != null) {
    		productDao.updateProductStatus(productDto);
    		System.out.println("상품기본정보 업데이트 완료!");
    		return 1;
    	}
    	System.out.println("업데이트 할 데이터가 없습니다.");
    	return 0;
    }
    
    // 상품 금리 업데이트
    public int updateRateStatus(ProductRateDto prdRateDto) {
    	if(prdRateDto != null) {
    		iProductRateDao.updateProductRate(prdRateDto);
    		System.out.println("상품 금리 업데이트 완료!");
    		return 1;
    	}
    	return 0;
    }
    
    // 상품 설명 업데이트
    public int updateProductDescription(ProductDescriptionDto prdDescDto) {
    	if(prdDescDto != null) {
    		iProductDescDao.updateProductDescription(prdDescDto);
    		System.out.println("상품 설명 업데이트완료!");
    		return 1;
    	}
    	System.out.println("업데이트 할 데이터가 안들어왔습니다!");
    	return 0;
    }
    
    // 상품 가입 조건 업데이트
    public int updateProductCondition(ProductConditionDto prdCndDto){
    	if(prdCndDto != null) {
    		iProductionDao.updateProductCondition(prdCndDto);
    		System.out.println("상품 가입 조건 업데이트 완료!");
    		return 1;
    	}
    	System.out.println("업데이트 할 데이터가 들어오지 않았습니다.");
    	return 0;
    }

}
