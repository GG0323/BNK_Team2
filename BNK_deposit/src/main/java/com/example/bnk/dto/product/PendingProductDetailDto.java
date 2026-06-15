package com.example.bnk.dto.product;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PendingProductDetailDto {
	
	private long product_no;
    private String product_name;
    private String product_type;
    private BigDecimal min_interest_rate;
    private BigDecimal max_interest_rate;
    private String interest_payment_type;
    private String interest_calc_type;
    private LocalDate sale_start_date;
    private LocalDate sale_end_date;
    private String product_status;
    private String branch_join_yn;
    private String internet_join_yn;
    private String mobile_join_yn;
    private LocalDate created_at;
    private LocalDate updated_at;

    // 부속 PK (없으면 null)
    private Long rate_no;
    private Long terms_no;
    private Long description_no;
    private Long condition_no;
	
	
}
