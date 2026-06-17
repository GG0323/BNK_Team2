package com.example.bnk.dto.product.ai;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProductPersonaRecommendItemDto {

    private long productNo;
    private String productName;
    private String productType;
    private String subtitle;

    private double minInterestRate;
    private double maxInterestRate;

    private long minJoinAmount;
    private long maxJoinAmount;

    private String branchJoinYn;
    private String internetJoinYn;
    private String mobileJoinYn;

    private int score;
    private int fitPercent;
    private int benefitChancePercent;

    private String reason;

    private List<String> evidence = new ArrayList<>();

    private String detailUrl;
}