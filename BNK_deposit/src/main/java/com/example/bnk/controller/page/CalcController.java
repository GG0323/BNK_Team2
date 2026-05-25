package com.example.bnk.controller.page;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bnk.service.common.CalcService;

import lombok.RequiredArgsConstructor;

@Controller
@Validated
@RequiredArgsConstructor
public class CalcController {
	
    private final CalcService calcService;

    @GetMapping("/calc/interest")
    @ResponseBody
    public Map<String, Object> calculateInterest(
            @RequestParam("amount") Long amount,
            @RequestParam("months") Integer months,
            @RequestParam("rate") Double rate,
            @RequestParam(value = "type", defaultValue = "deposit") String type,
            // 아래 파라미터를 추가로 받아야 합니다.
            @RequestParam(value = "interestType", defaultValue = "simple") String interestType) { 
        
        return calcService.calculateFutureValue(amount, months, rate, type, interestType); 
    }
    
    @GetMapping("/calc/popup")
    public String showCalcPopup() {
        return "common/calculator"; 
    }
}