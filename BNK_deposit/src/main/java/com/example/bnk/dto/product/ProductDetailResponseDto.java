package com.example.bnk.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDetailResponseDto {

	//  join 쿼리문 사용하기 위해 만든 DTO입니다.
	private ProductDto product;
	private ProductRateDto rate;
    private ProductDescriptionDto description;
    private ProductConditionDto condition;
    
}
