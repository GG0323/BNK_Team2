package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finance")
public class FinanceDictionaryPageController {

    // 금융용어 사전 목록 화면
    @GetMapping("/financedictionary")
    public String dictionaryPage() {
        return "common/financedictionary";
    }

    // 금융용어 사전 상세 화면
    @GetMapping("/financedictionary/{dictionary_no}")
    public String dictionaryDetailPage(@PathVariable("dictionary_no") long dictionaryNo) {
        return "common/financedictionarydetail";
    }
    
    // 직원용 금융용어 사전 관리 화면
    @GetMapping("/employee/financedictionary")
    public String employeeDictionaryManagePage() {
        return "Employees/staff/financedictionary/employee_financedictionary";
    }

    // 직원용 금융용어 등록 화면
    @GetMapping("/financedictionary/write")
    public String dictionaryWritePage() {
        return "Employees/staff/financedictionary/financedictionary_write";
    }

    // 직원용 금융용어 수정 화면
    @GetMapping("/financedictionary/edit/{dictionary_no}")
    public String dictionaryEditPage(@PathVariable("dictionary_no") long dictionaryNo) {
        return "Employees/staff/financedictionary/financedictionary_edit";
    }
}