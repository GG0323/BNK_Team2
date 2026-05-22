package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor	
public class EmployeeDetailUpdateDto {
	
	private long employee_no;		// 직원 PK

	private String employee_name;	// 직원명

	private String phone_number;	// 전화번호
	private String email;			// 이메일

	private String job_title;		// 직급
	private String status;			// 재직상태  ('ACTIVE', 'LEAVE', 'RESIGNED')
	private LocalDate updated_at;	// 수정일자
	
}
