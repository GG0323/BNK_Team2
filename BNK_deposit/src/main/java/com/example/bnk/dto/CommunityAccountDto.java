package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor						// 커뮤니티 전용 계정
public class CommunityAccountDto {
	private long community_account_no;	// 커뮤니티 계정 PK
	private long member_no;				// 회원 FK
	private long employee_no;			// 직원 FK
	private String nickname;			// 커뮤니티 닉네임
	private String account_role;		// 계정 권한 ('MEMBER','EMPLOYEE','ADMIN')
	private String community_status;	// 커뮤니티 계정 상태 ('ACTIVE', 'SUSPENDED','WITHDRAWN')
	private LocalDate created_at;		// 생성일시
	private LocalDate updated_at;		// 수정일시
}
