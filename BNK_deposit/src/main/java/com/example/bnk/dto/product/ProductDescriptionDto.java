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
public class ProductDescriptionDto {
	// 상품 설명~
	
	public long description_no;                  // 상품설명 PK
	public long product_no;                      // 상품 FK
	public String subtitle;                      // 부제목
	public String content;                       // 홍보문구
	public String eligibility_desc;              // 가입자격
	public String period_desc;                   // 가입기간
	public String amount_desc;                   // 가입금액
	public String preferential_rate_summary;     // 상단 우대금리 요약
	public String product_feature_desc;          // 상품특징
	public String deposit_subject_desc;          // 예금과목
	public String payment_method_desc;           // 납입방법
	public String join_method_desc;              // 가입방법
	public String required_document_desc;        // 필요서류
	public String  tax_benefit_desc;             // 세제혜택
	public String principal_interest_limit_desc; // 원금 또는 이자지급제한
	public String  interest_rate_desc;           // 적용금리
	public String interest_payment_desc;         // 이자지급방식
	public String preferential_rate_desc;        // 우대이율
	public String expected_interest_desc;        // 예상수취이자액 안내
	public String caution_note;                  // 유의사항
	public String reference_note;                // 참고사항
	public String sale_period_desc;              // 판매기간문구
	public String non_taxable_savings_desc;      // 비과세종합저축
	public String disclosure_approval_desc;      // 공시승인번호
	public String image_url;                     // 이미지 URL
	public LocalDate created_at;                 // 생성일 DEFAULT SYSDATE
	public LocalDate updated_at;                 // 수정일

}
