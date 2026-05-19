package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor		
public class EmployeeLoginDto {
	private long employee_no;		// 직원 PK
	private String password_hash;	// 비밀번호
}
