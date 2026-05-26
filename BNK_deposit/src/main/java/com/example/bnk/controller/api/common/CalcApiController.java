package com.example.bnk.controller.api.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.service.common.CalcService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calc")
@RequiredArgsConstructor
public class CalcApiController {

    private final CalcService calcService;

    // 상품 화면 등 서버 계산이 필요한 경우 사용하는 API
    @GetMapping("/interest")
    public Map<String, Object> calculateInterest(
            @RequestParam("amount") Long amount,
            @RequestParam("months") Integer months,
            @RequestParam("rate") Double rate,
            @RequestParam(value = "type", defaultValue = "deposit") String type,
            @RequestParam(value = "interestType", defaultValue = "simple") String interestType) {

        return calcService.calculateFutureValue(amount, months, rate, type, interestType);
    }
}