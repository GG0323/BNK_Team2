package com.example.bnk.dto.employee;

import java.io.File;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor		
public class EmployeeRegistDto {
	// 회원가입용 dto
	
	private long dept_no;			// 부서 FK
	private String login_id;		// 직원 로그인 ID
	private String unHashPassword;	// 비밀번호
	private String employee_name;	// 직원명
	private String gender;			// 성별
	private LocalDate birth_date;	// 생년월일
	
	private String phone_number1;	// 전화번호1
	private String phone_number2;	// 전화번호2
	private String phone_number3;	// 전화번호3
	
	
	
	private String email;			// 이메일
	private String home_address;	// 집주소
	private LocalDate hire_date;	// 입사일
	private String job_title;		// 직급
	private String employee_role;	// 권한
	
	private File img;			// 프로필 사진

	
	
}
