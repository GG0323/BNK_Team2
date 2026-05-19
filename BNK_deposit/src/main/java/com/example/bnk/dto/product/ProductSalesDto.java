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
public class ProductSalesDto {
	// 상품 판매 Dto
	// p가 붙으면 가입을 진행하게 되면 작성될 내용
	
	private long subscription_no;         //p pk
	private long member_no;               //p 회원 FK
	private long product_no;              //p 상품 FK
	private long account_no;              //  가입 후 생성된 계좌 FK
	private String join_channel;          //p 가입 채널  BRANCH: 영업점 WEB: 인터넷 MOBILE: 모바일 OTHER: 기타
	private double applied_interest_rate; //  실제 적용 금리
	private long subscription_months;     //p 가입 개월 수
	private long subscription_amount;     //p 가입 금액
	private long auto_transfer_amount;    //p 자동이체 금액
	private long linked_account_no;       //p 자동이체 출금 계좌번호
	private LocalDate subscribed_at;      //  가입일시
	private LocalDate maturity_date;      //  만기일
	private String subscription_status;   //p 가입상태 DEFAULT 'DRAFT'  ('DRAFT','COMPLETE','EXPIRED')
	private long required_terms_agreed;   //p 필수 약관 동의 여부 DEFAULT 0 (0,1) 0: 미동의
	private long optional_terms_agreed;   //p 선택 약관 동의 여부 DEFAULT 0 (0,1) 0: 미동의
	private LocalDate created_at;         //  생성일 DEFAULT SYSDATE
	private LocalDate updated_at;         //p 수정일
	
}
