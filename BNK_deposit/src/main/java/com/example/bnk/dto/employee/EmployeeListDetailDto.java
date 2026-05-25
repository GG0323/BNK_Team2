package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor		
public class EmployeeListDetailDto { // 수정
	
	private long employee_no;		// 직원 PK
	private long dept_no;			// 부서 FK
	private String dept_name;// join 부서 테이블 조인
	
	private String dept_code;		// 부서코드
	private long parent_dept_id;	// 상위부서 FK
	private String dept_phone;		// 부서 대표번호
	private String dept_location;	// 부서 위치
	private String dept_status;		// 사용 여부 ('ACTIVE', 'INACTIVE')
	
	private String employee_name;	// 직원명
	private String job_title;		// 직급
	private String gender;			// 성별
	private LocalDate birth_date;	// 생년월일
	private String phone_number;	// 전화번호
	private String email;			// 이메일
	private String home_address;	// 집주소
	private LocalDate hire_date;	// 입사일
	private String employee_role;	// 권한
	private String status;			// 재직상태  ('ACTIVE', 'LEAVE', 'RESIGNED')
	private String img_url;			// 프로필 사진
	private LocalDate updated_at;	// 수정일자

}
