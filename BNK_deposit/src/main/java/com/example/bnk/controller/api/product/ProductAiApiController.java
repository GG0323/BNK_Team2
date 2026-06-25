package com.example.bnk.controller.api.product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendRequestDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendResponseDto;
import com.example.bnk.service.product.ProductViewService;
import com.example.bnk.service.product.ai.ProductFastApiClient;
import com.example.bnk.service.product.ai.ProductPersonaRecommendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products/ai")
@RequiredArgsConstructor
public class ProductAiApiController {

    private final ProductPersonaRecommendService productPersonaRecommendService;
    private final ProductViewService productViewService;
    private final ProductFastApiClient productFastApiClient;

    @PostMapping("/recommend")
    public Map<String, Object> recommend(@RequestBody ProductPersonaRecommendRequestDto request) {

        if (request == null) {
            request = new ProductPersonaRecommendRequestDto();
        }

        ProductPersonaRecommendResponseDto fastApiResponse =
                requestFastApiRecommend(request);

        if (hasRecommendResult(fastApiResponse)) {
            System.out.println("[상품 AI] FastAPI 추천 결과 사용");
            return success(fastApiResponse);
        }

        System.out.println("[상품 AI] FastAPI 추천 실패 또는 결과 없음. 기존 Spring 추천 로직 사용");

        ProductPersonaRecommendResponseDto fallbackResponse =
                productPersonaRecommendService.recommend(request);

        return success(fallbackResponse);
    }

    private ProductPersonaRecommendResponseDto requestFastApiRecommend(
            ProductPersonaRecommendRequestDto request) {

        try {
            List<ProductListViewDto> productList =
                    productViewService.getProductList("maxRateDesc", "ALL");

            if (productList == null || productList.isEmpty()) {
                return null;
            }

            List<ProductDetailViewDto> productDetailList = new ArrayList<>();

            for (ProductListViewDto product : productList) {
                if (product == null) {
                    continue;
                }

                ProductDetailViewDto detail =
                        productViewService.getProductDetail(product.getProduct_no());

                if (detail != null) {
                    productDetailList.add(detail);
                }
            }

            if (productDetailList.isEmpty()) {
                return null;
            }

            return productFastApiClient.recommend(request, productDetailList);

        } catch (Exception e) {
            System.out.println("FastAPI 맞춤 상품 추천 호출 실패. 기존 Spring 추천 로직 사용. error="
                    + e.getMessage());
            return null;
        }
    }

    private boolean hasRecommendResult(ProductPersonaRecommendResponseDto response) {
        return response != null
                && response.getRecommendedProducts() != null
                && !response.getRecommendedProducts().isEmpty();
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        return body;
    }
}