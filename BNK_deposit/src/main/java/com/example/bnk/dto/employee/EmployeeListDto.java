package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data @ToString
@NoArgsConstructor
@AllArgsConstructor		
public class EmployeeListDto {
	
	private long employee_no;		// 직원 PK
	private long dept_no;			// 부서 FK
	private String dept_name;// join 부서 테이블 조인
	private String employee_name;	// 직원명
	private String job_title;		// 직급
	private String status;			// 재직상태  ('ACTIVE', 'LEAVE', 'RESIGNED')
	

}
