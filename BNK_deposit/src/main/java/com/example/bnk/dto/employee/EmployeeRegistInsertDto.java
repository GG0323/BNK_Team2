package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.Data;
import lombok.ToString;

@Data @ToString
public class EmployeeRegistInsertDto {
	private long dept_no;			// 부서 FK
	private String login_id;		// 직원 로그인 ID
	private String password_hash;	// 비밀번호
	private String employee_name;	// 직원명
	private String gender;			// 성별
	private LocalDate birth_date;	// 생년월일
	private String phone_number;	// 전화번호
	private String email;			// 이메일
	private String home_adress;	    // 집주소 
	private LocalDate hire_date;	// 입사일
	private String job_title;		// 직급
	private String employee_role;	// 권한
	private String status;
	private String img_url;			// 프로필 사진
	
	
	public EmployeeRegistInsertDto (EmployeeRegistDto empRegistDto, String password_hash, String phone_number, String img_url) {
		this.dept_no = empRegistDto.getDept_no();
		this.login_id = empRegistDto.getLogin_id();		
		this.password_hash = password_hash;	// 
		this.employee_name = empRegistDto.getEmployee_name();
		this.gender = empRegistDto.getGender();		
		this.birth_date = empRegistDto.getBirth_date();
		this.phone_number = phone_number;	// 
		this.email = empRegistDto.getEmail();	
		this.home_adress = empRegistDto.getHome_address();
		this.hire_date = empRegistDto.getHire_date();
		this.job_title = empRegistDto.getJob_title();
		this.employee_role = empRegistDto.getEmployee_role();
		this.status = "ACTIVE";
		this.img_url = img_url;		        // 

	}
	
	
	
}
