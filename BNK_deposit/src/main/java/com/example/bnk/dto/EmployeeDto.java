package com.example.bnk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor					// 직원
public class EmployeeDto {
	private long employee_no;		// 직원 PK
	private long dept_no;			// 부서 FK
	private String login_id;		// 직원 로그인 ID
	private String password_hash;	// 비밀번호
	private String employee_name;	// 직원명
	private String gender;			// 성별
	private LocalDate birth_date;	// 생년월일
	private String phone_number;	// 전화번호
	private String email;			// 이메일
	private String home_address;	// 집주소
	private LocalDate hire_date;	// 입사일
	private String job_title;		// 직급
	private String employee_role;	// 권한
	private String status;			// 재직상태  ('ACTIVE', 'LEAVE', 'RESIGNED')
	private String img_url;			// 프로필 사진
	private LocalDate updated_at;	// 수정일자
}
