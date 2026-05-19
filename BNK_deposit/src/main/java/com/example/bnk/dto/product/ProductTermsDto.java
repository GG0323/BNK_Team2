package com.example.bnk.dto.product;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductTermsDto {
	// 상품 약관 Dto
	
	private long terms_no;          // 약관 PK
	private long product_no;        // 상품 FK
	private String terms_title;     // 약관명
	private String terms_type;      // 약관 구분 REQUIRED: 필수 OPTIONAL: 선택
	private String pdf_url;         // 약관 PDF 경로
	private String image_url;       // 약관 JPG 경로
	private String terms_summary;   // 약관 요약
	private String terms_version;   // 약관 버전 DEFAULT '1.0'
	private String use_yn;          // 사용 여부 DEFAULT 'Y' Y: 사용 N: 미사용
	private long uploaded_by;       // 등록 직원 FK
	private LocalDate uploaded_at;  // 등록일시 DEFAULT SYSDATE
	private LocalDate updated_at;   // 마지막 수정일시
	
}
