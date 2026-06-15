package com.example.bnk.service.product;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.product.IProductSalesDao;
import com.example.bnk.dto.member.MemberProductDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductSalesService {

    private final IProductSalesDao productSalesDao;

    // 마이페이지에서 사용자가 가입한 상품의 수 조회
    public int getSubscribedProductCount(long memberNo) {
        return productSalesDao.countProductSalesByMemberNo(memberNo);
    }
    
    // 사용자가 가입한 상품의 수 출력
    public List<MemberProductDto> getSubscribedProducts(String username) {
        return productSalesDao.findSubscribedProductsByMemberNo(username);
    }
    
	
    // 커뮤니티 가입 시 우대금리 상승 서비스
	public int upTermscommunityRegist(@Param("member_no") long member_no) {
		int result = productSalesDao.upTermsCommunityRegist(member_no);
		
		if(result == 1) {
			System.out.println("우대금리 상승 성공");
			return 1;
		}
		System.out.println("우대금리 상승 실패");
		return 0;
	}
}