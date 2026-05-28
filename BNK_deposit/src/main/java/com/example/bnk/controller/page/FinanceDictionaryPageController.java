package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FinanceDictionaryPageController {

    // 일반 사용자용 금융용어 사전 목록 화면
    @GetMapping("/finance/financedictionary")
    public String dictionaryPage() {
        return "common/financedictionary";
    }

    // 일반 사용자용 금융용어 사전 상세 화면
    // 목록에서 팝업창으로 열리는 조회 전용 페이지
    @GetMapping("/finance/financedictionary/{dictionary_no}")
    public String dictionaryDetailPage(@PathVariable("dictionary_no") long dictionaryNo) {
        return "common/financedictionarydetail";
    }

    // 직원용 금융용어 사전 관리 화면
    // 직원 사이드바와 staffPage 링크 기준에 맞춰 /employee 로 둔다.
    @GetMapping("/employee/financedictionary")
    public String employeeDictionaryManagePage() {
        return "Employees/staff/financedictionary/employee_financedictionary";
    }

    // 직원용 금융용어 등록 화면
    @GetMapping("/finance/financedictionary/write")
    public String dictionaryWritePage() {
        return "Employees/staff/financedictionary/financedictionary_write";
    }

    // 직원용 금융용어 수정 화면
    @GetMapping("/finance/financedictionary/edit/{dictionary_no}")
    public String dictionaryEditPage(@PathVariable("dictionary_no") long dictionaryNo) {
        return "Employees/staff/financedictionary/financedictionary_edit";
    }
}