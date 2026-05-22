package com.example.bnk.service.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bnk.dao.product.IProductSalesDao;
import com.example.bnk.dto.member.MemberProductDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSalesService {

    private final IProductSalesDao productSalesDao;

    // 마이페이지에서 사용자가 가입한 상품의 수 조회
    public int getSubscribedProductCount(long memberNo) {
        return productSalesDao.countProductSalesByMemberNo(memberNo);
    }
    
    // 사용자가 가입한 상품의 수 출력
    public List<MemberProductDto> getSubscribedProducts(long memberNo) {
        return productSalesDao.findSubscribedProductsByMemberNo(memberNo);
    }
}