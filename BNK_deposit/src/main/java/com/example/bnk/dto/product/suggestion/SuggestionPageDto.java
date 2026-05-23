package com.example.bnk.dto.product.suggestion;

import java.util.List;

import com.example.bnk.dto.employee.EmployeeDto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SuggestionPageDto {
    private EmployeeDto myInfo;           // 로그인한 직원 정보
    private List<EmployeeDto> managers;   // 관리자 목
}
