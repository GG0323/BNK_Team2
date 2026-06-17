package com.example.bnk.dto.product.ai;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProductPersonaRecommendResponseDto {

    private String summary;

    private List<ProductPersonaRecommendItemDto> recommendedProducts = new ArrayList<>();
}