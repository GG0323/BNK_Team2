package com.example.bnk.dto.employee;

import java.time.LocalDate;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmployeeLogSelectDto {
	
	
    // 누가
    private Long   employee_no;

    // 무엇을
    private String action_type;       // 드롭다운
    private String target_table;      // 드롭다운

    private Integer response_status;  // 200 / 403 / 500

    // 어디서
    private String request_ip;        // IP 검색
	
    // 언제
    private LocalDate from_date;
    private LocalDate to_date;
    
    
}
