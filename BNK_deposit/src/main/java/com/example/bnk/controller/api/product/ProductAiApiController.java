package com.example.bnk.controller.api.product;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.product.ai.ProductPersonaRecommendRequestDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendResponseDto;
import com.example.bnk.service.product.ai.ProductPersonaRecommendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products/ai")
@RequiredArgsConstructor
public class ProductAiApiController {

    private final ProductPersonaRecommendService productPersonaRecommendService;

    @PostMapping("/recommend")
    public Map<String, Object> recommend(@RequestBody ProductPersonaRecommendRequestDto request) {
        ProductPersonaRecommendResponseDto response =
                productPersonaRecommendService.recommend(request);

        return success(response);
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        return body;
    }
}