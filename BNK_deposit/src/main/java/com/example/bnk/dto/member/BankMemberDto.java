package com.example.bnk.dto.member;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor						// 은행 회원
public class BankMemberDto {
	private long member_no;				// 회원 PK
	private String login_id;			// 로그인 아이디
	private String password_hash;		// 비밀번호
	private String member_name;			// 회원명
	private String member_type;			// 회원구분 ( 'PERSONAL', 'BUSINESS')
	private String member_identifier;	// 회원 식별번호 (주민등록번호, 사업자번호)
	private String gender;				// 성별	| M: MALE | F: FEMALE | 기업회원은 NULL 가능
	private LocalDate birth_date;		// 생년월일 / 개업일자
	private String phone_number;		// 전화번호
	private String email;				// 이메일
	private String adress;				// 주소
	private int credit_score;			// 신용 점수
	private String member_status;		// 회원 상태 | ASSOCIATE: 준회원 | REGULAR: 정회원 | DORMANT: 휴면 | WITHDRAWN: 탈퇴
	private LocalDate created_at;		// 생성일시
	private LocalDate update_at;		// 수정일시
	private LocalDate last_login_at;	// 마지막 로그인일시
}
