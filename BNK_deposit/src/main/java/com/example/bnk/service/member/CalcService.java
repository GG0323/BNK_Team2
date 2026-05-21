package com.example.bnk.service.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class CalcService {

    // 금융 공통 상수: 일반과세 15.4% (이자소득세 14% + 지방소득세 1.4%)
    private static final double TAX_RATE = 0.154;

    /**
     * 1. 목돈 굴리기 (만기 수령액 계산)
     * @param interestType "simple"(단리) 또는 "compound"(복리)
     */
    public Map<String, Object> calculateFutureValue(Long amount, Integer months, Double rate, String type, String interestType) {
        double r = rate / 100.0; 
        double mRate = r / 12.0; // 월 이율
        
        double principalTotal = 0; 
        double interest = 0; 

        if ("deposit".equals(type)) {
            principalTotal = amount;
            if ("compound".equals(interestType)) {
                // 예금 복리 공식: 원금 * (1+월이율)^개월수 - 원금
                interest = amount * Math.pow(1 + mRate, months) - amount;
            } else {
                // 예금 단리 공식
                interest = amount * r * (months / 12.0);
            }
        } else if ("savings".equals(type)) {
            principalTotal = amount * months;
            if ("compound".equals(interestType)) {
                // 적금 복리(기수불) 공식: 월납입액 * (((1+월이율)^개월수 - 1) / 월이율) * (1+월이율) - 원금총액
                interest = amount * ((Math.pow(1 + mRate, months) - 1) / mRate) * (1 + mRate) - principalTotal;
            } else {
                // 적금 단리 공식
                interest = amount * (months * (months + 1) / 2.0) * mRate; 
            }
        }

        double tax = interest * TAX_RATE;

        Map<String, Object> resultMap = new HashMap<>();
        
        resultMap.put("genPrincipal", Math.round(principalTotal));
        resultMap.put("genPreTax", Math.round(interest));
        resultMap.put("genTax", Math.round(tax));
        resultMap.put("genTotal", Math.round(principalTotal + interest - tax));

        resultMap.put("freePrincipal", Math.round(principalTotal));
        resultMap.put("freePreTax", Math.round(interest));
        resultMap.put("freeTax", 0);
        resultMap.put("freeTotal", Math.round(principalTotal + interest));

        return resultMap;
    }

    /**
     * 2. 목돈 모으기 (목표 금액 달성을 위한 필요 납입액 역산)
     * @param interestType "simple"(단리) 또는 "compound"(복리)
     */
    public Map<String, Object> calculateRequiredAmount(Long targetAmount, Integer months, Double rate, String type, String interestType) {
        double r = rate / 100.0;
        double mRate = r / 12.0;
        
        double requiredGenAmount = 0; 
        double requiredFreeAmount = 0; 

        if ("deposit".equals(type)) {
            double interestMultiplier = 0;
            if ("compound".equals(interestType)) {
                interestMultiplier = Math.pow(1 + mRate, months) - 1;
            } else {
                interestMultiplier = r * (months / 12.0);
            }
            
            requiredGenAmount = targetAmount / (1 + interestMultiplier * (1 - TAX_RATE));
            requiredFreeAmount = targetAmount / (1 + interestMultiplier);
            
        } else if ("savings".equals(type)) {
            double interestMultiplier = 0;
            if ("compound".equals(interestType)) {
                // 총 이자 비율을 구하기 위한 승수
                interestMultiplier = ((Math.pow(1 + mRate, months) - 1) / mRate) * (1 + mRate) - months;
            } else {
                interestMultiplier = (months * (months + 1) / 2.0) * mRate;
            }
            
            requiredGenAmount = targetAmount / (months + interestMultiplier * (1 - TAX_RATE));
            requiredFreeAmount = targetAmount / (months + interestMultiplier);
        }

        Map<String, Object> resultMap = new HashMap<>();
        
        resultMap.put("requiredGenAmount", Math.round(requiredGenAmount));
        resultMap.put("requiredTaxFreeAmount", Math.round(requiredFreeAmount));

        return resultMap;
    }
}
