package com.example.bnk.service.product;

import org.springframework.stereotype.Service;
import com.example.bnk.dao.product.IProductSalesDao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSalesService {

    private final IProductSalesDao productSalesDao;

    public int getSubscribedProductCount(long memberNo) {
        return productSalesDao.countProductSalesByMemberNo(memberNo);
    }
}