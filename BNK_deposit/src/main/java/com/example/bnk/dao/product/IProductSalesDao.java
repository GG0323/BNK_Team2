package com.example.bnk.dao.product;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IProductSalesDao {

	int countProductSalesByMemberNo(long memberNo);

}
