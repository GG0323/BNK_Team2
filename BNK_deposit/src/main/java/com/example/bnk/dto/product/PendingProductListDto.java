package com.example.bnk.dto.product;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PendingProductListDto {
	
	private long product_no;
    private String product_name;
    private String product_type;
    private LocalDate created_at;

    // 부속 정보 존재 여부 ('Y'/'N')
    private String has_join_condition;
    private String has_description;
    private String has_interest_rate;
    private String has_terms;
	
	
	
}
