package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor					// 부서
public class DeptDto {
	private long dept_id;			// 부서 PK
	private String dept_name;		// 부서명
	private String dept_code;		// 부서코드
	private long parent_dept_id;	// 상위부서 FK
	private String dept_phone;		// 부서 대표번호
	private String dept_location;	// 부서 위치
	private String dept_status;		// 사용 여부 ('ACTIVE', 'INACTIVE')
	private LocalDate created_at;	// 생성일
	private LocalDate updated_at;	// 수정일
}
