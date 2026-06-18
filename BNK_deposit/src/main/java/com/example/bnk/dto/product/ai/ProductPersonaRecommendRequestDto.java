package com.example.bnk.dto.product.ai;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProductPersonaRecommendRequestDto {

    // 사용자 기본 조건
    private int age;
    private long balance;
    private long monthlyAmount;
    private int periodMonths;

    // 사용자 페르소나 조건
    private String purpose;              // MAKE_MONEY / ROLL_MONEY / HIGH_RATE / EMERGENCY
    private String preferredProductType; // ALL / DEPOSIT / SAVINGS
    private String preferredChannel;     // ALL / MOBILE / INTERNET / BRANCH

    // 관심 조건
    private List<String> interestConditions = new ArrayList<>();

    public String getPurposeSafe() {
        return purpose == null || purpose.trim().isEmpty() ? "MAKE_MONEY" : purpose;
    }

    public String getPreferredProductTypeSafe() {
        return preferredProductType == null || preferredProductType.trim().isEmpty() ? "ALL" : preferredProductType;
    }

    public String getPreferredChannelSafe() {
        return preferredChannel == null || preferredChannel.trim().isEmpty() ? "ALL" : preferredChannel;
    }

    public List<String> getInterestConditionsSafe() {
        return interestConditions == null ? new ArrayList<>() : interestConditions;
    }
}