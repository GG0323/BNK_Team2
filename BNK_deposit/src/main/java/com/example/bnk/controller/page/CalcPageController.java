package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalcPageController {

    // 일반 금융계산기 팝업 화면
    @GetMapping("/calc/popup")
    public String showCalcPopup() {
        return "common/calculator";
    }
}