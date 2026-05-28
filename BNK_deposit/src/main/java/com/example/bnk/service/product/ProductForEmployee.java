package com.example.bnk.service.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dao.product.IApporvedSuggestionDao;
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
import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductForEmployee {
	
	private final IApporvedSuggestionDao iAprSuggestionDao;
	
	private final IProductDao productDao;
	private final IProductConditionDao iProductCondDao;
	private final IProductRateDao iProductRateDao;
	private final IProductDescriptionDao iProductDescDao;
	private final IProductDetailResponseDao iProductDetailResponseDao;
	
	// 이미지 저장용
	@Value("${file.upload.path}")
    private String uploadPath; // C:/upload/
    @Value("${file.upload.url}")
    private String uploadUrl;  // /upload/
	
	
    // 1차 승인 상품 목록 조회
    public List<ApprovedSuggestionDetailDto> showAllApprovedSuggestionList(){
    	System.out.println("1차 제안에서 승인된 상품들을 불러오겠습니다.");
    	
    	return iAprSuggestionDao.approvedSuggestionList();
    }
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
	
	// 특정 approved suggestion 불러오기
	public ApprovedSuggestionDetailDto selectApprovedSug(@Param("suggestion_no") long suggestion_no) {
		return iAprSuggestionDao.selectApprovedSug(suggestion_no);
	}
	
	
	/* 특정 조회
	------------------------------------------------------------------------------*/
	// con
	public ProductConditionDto selectConditionPrd(@Param("condition_no") long condition_no) {
		return iProductCondDao.selectConditionPrd(condition_no);
	}
	
	// rate
	public ProductRateDto selectRatePrd(@Param("rate_no") long rate_no) {
		return iProductRateDao.selectRatePrd(rate_no);
	}
	
	// des
	public ProductDescriptionDto selectDescriptionPrd(@Param("description_no") long description_no) {
		return iProductDescDao.selectDescriptionPrd(description_no);
	}
	
	/* 등록 서비스
	---------------------------------------------------------------------------------------------------*/
	// 상품 조건 등록 서비스
	@Transactional // 두 개 이상의 CUD 작업이 일어나므로 트랜잭션 보장 필수!
	public int insertAllCondition(ProductConditionDto prdCndDto,
								  @RequestParam("suggestion_no") long suggestion_no) {
		if(prdCndDto != null) {
			if("ALL".equals(prdCndDto.getGender())) {
				prdCndDto.setGender(null);
			}else {
				System.out.println("All 변환 실패!");					
			}
			// 1단계: TB_PRODUCT_CONDITION 테이블에 인서트 시도
			// (위 1번 작업 덕분에 이 쿼리가 성공하면 prdCndDto 안에 condition_no가 자동으로 채워짐요!)
			int insertResult = iProductCondDao.insertAllCondition(prdCndDto);
			
			// BEGIN / END 사용하면 성공 시 -1 반환되용
			if(insertResult == -1) {
				// condition_no 추출!(리턴 받음)
				long condition_no = prdCndDto.getCondition_no();
				
				System.out.println("condition_no: " + condition_no);
				System.out.println("목표 suggestion_no: " + suggestion_no);
				// 3단계: 중간 테이블(TB_APPROVED_SUGGESTION)에 condition_no 업데이트하기
				int updateResult = iAprSuggestionDao.updateAprToCondition(suggestion_no, condition_no);
				
				if(updateResult == 1) {
					return 1; // 인서트와 중간 테이블 업데이트 모두 성공!
				} else {
					System.out.println("가입조건은 등록되었으나 중간 테이블 업데이트에 실패했습니다.");
					return 0;
				}
				
			} else {
				System.out.println("DB를 넣는 과정 중 오류가 발생했습니다.");
				return 0;
			}
		}
		
		return 0;
	}
	
	// 상품 설명 등록 서비스!
	public int insertAllDescription(ProductDescriptionDto prdDescDto,
									@RequestParam("suggestion_no")long suggestion_no) {
		if(prdDescDto != null) {
			
			int insertResult = iProductDescDao.insertAllDescription(prdDescDto);
			System.out.println("insertResult: " + insertResult);
			if(insertResult == -1) {
				long description_no = prdDescDto.getDescription_no();
				
				System.out.println("descriptoin_no: " + description_no);
				System.out.println("목표 suggestion_no: " + suggestion_no);
				
				int updateResult = iAprSuggestionDao.updateAprToDescription(suggestion_no, description_no);
				
				if(updateResult == 1) {
					return 1;
				}else {
					System.out.println("가입 조건은 등록되었으나 중간 테이블 업데이트에 실패했습니다.");
					return 0;
				}
				
			}else {
				System.out.println("DB를 넣는 과정 중 오류가 발생했습니다.");
				return 0;
			}

		}
		System.out.println("그냥 Dto가 안들어있음 ㅇㅇ...");
		return 0;
	}
	
	// 상품 금리 등록 서비스
	public int insertAllRate(ProductRateDto prdRateDto, @RequestParam("suggestion_no") long suggestion_no) {
		if(prdRateDto != null) {
			
			int insertResult = iProductRateDao.insertAllRate(prdRateDto);
			
			if(insertResult == -1) {
				long rate_no = prdRateDto.getRate_no();
				
				System.out.println("Rate_no: " + rate_no);
				System.out.println("목표suggestion_no: " + suggestion_no);
				
				int updateResult = iAprSuggestionDao.updateAprToRate(suggestion_no, rate_no);
				
				if(updateResult == 1) {
					System.out.println("APPROVED 테이블 업데이트 성공");
					return 1;
				}else {
					System.out.println("UPDATE 'APPROVED TABLE' FAILED...");
					return 0;
				}
				
			}else {
				System.out.println("DB를 넣는 과정 중 오류가 발생했습니다.");
				return 0;
			}
			
		}
		System.out.println("그냥 DTO가 안들어있는데?");
		return 0;
	}

	// 상품 설명 등록 서비스
    @Transactional
    public int saveDescription(ProductDescriptionDto prdDescDto,
    		 @RequestParam("suggestion_no") long suggestion_no	) throws IOException {

        
        MultipartFile file = prdDescDto.getImage_file();
        
        if (file != null && !file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            
            // 1. 진짜 물리적 경로(uploadPath)에 파일 저장
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs();
                System.out.println("파일 업로드 성공");
            }
            
            File destination = new File(folder, savedFileName);
            file.transferTo(destination);
            
            // 2. DB에는 웹에서 읽어갈 수 있는 주소 경로(uploadUrl)를 조합해서 저장!
            // 예: "/upload/" + "uuid_file.png" => /upload/uuid_file.png
            prdDescDto.setImage_url(uploadUrl + savedFileName);
            System.out.println(uploadUrl + savedFileName);
        }
        
        if(insertAllDescription(prdDescDto, suggestion_no) == 1) {
        	return 1;
        }
        
        return 0;
    }
    
    
    
    /* 업데이트
    ----------------------------------------------------------------------------------------------*/
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
    		iProductCondDao.updateProductCondition(prdCndDto);
    		System.out.println("상품 가입 조건 업데이트 완료!");
    		return 1;
    	}
    	System.out.println("업데이트 할 데이터가 들어오지 않았습니다.");
    	return 0;
    }

}
