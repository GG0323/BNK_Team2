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

    private static final int FAST_API_CANDIDATE_LIMIT = 12;

    private final ProductPersonaRecommendService productPersonaRecommendService;
    private final ProductFastApiClient productFastApiClient;
    private final ProductViewService productViewService;

    @PostMapping("/recommend")
    public Map<String, Object> recommend(@RequestBody ProductPersonaRecommendRequestDto request) {
        if (request == null) {
            request = new ProductPersonaRecommendRequestDto();
        }

        ProductPersonaRecommendResponseDto fastApiResponse = requestFastApiRecommend(request);

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
            ProductPersonaRecommendRequestDto request
    ) {
        List<ProductDetailViewDto> candidateProducts = loadFastApiCandidateProducts(request);

        if (candidateProducts.isEmpty()) {
            System.out.println("[상품 AI] FastAPI 추천 후보 상품 없음");
            return null;
        }

        System.out.println("[상품 AI] FastAPI 추천 후보 상품 수 = " + candidateProducts.size());

        return productFastApiClient.createRecommend(request, candidateProducts);
    }

    private List<ProductDetailViewDto> loadFastApiCandidateProducts(ProductPersonaRecommendRequestDto request) {
        List<ProductDetailViewDto> candidates = new ArrayList<>();

        try {
            String productType = getProductTypeFilter(request);

            List<ProductListViewDto> productList =
                    productViewService.getProductList("maxRateDesc", productType);

            if (productList == null || productList.isEmpty()) {
                return candidates;
            }

            for (ProductListViewDto product : productList) {
                if (product == null) {
                    continue;
                }

                ProductDetailViewDto detail =
                        productViewService.getProductDetail(product.getProduct_no());

                if (detail == null) {
                    continue;
                }

                if (!matchesPreferredChannel(detail, request)) {
                    continue;
                }

                candidates.add(detail);

                if (candidates.size() >= FAST_API_CANDIDATE_LIMIT) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("[상품 AI] 추천 후보 상품 조회 실패. message=" + e.getMessage());
        }

        return candidates;
    }

    private String getProductTypeFilter(ProductPersonaRecommendRequestDto request) {
        if (request == null) {
            return "ALL";
        }

        String productType = request.getPreferredProductTypeSafe();

        if (productType == null || productType.trim().isEmpty()) {
            return "ALL";
        }

        productType = productType.trim().toUpperCase();

        if ("DEPOSIT".equals(productType) || "SAVINGS".equals(productType)) {
            return productType;
        }

        return "ALL";
    }

    private boolean matchesPreferredChannel(
            ProductDetailViewDto product,
            ProductPersonaRecommendRequestDto request
    ) {
        if (product == null || request == null) {
            return true;
        }

        String preferredChannel = request.getPreferredChannelSafe();

        if (preferredChannel == null || preferredChannel.trim().isEmpty()) {
            return true;
        }

        preferredChannel = preferredChannel.trim().toUpperCase();

        if ("ALL".equals(preferredChannel)) {
            return true;
        }

        if ("MOBILE".equals(preferredChannel)) {
            return "Y".equalsIgnoreCase(product.getMobile_join_yn());
        }

        if ("INTERNET".equals(preferredChannel)) {
            return "Y".equalsIgnoreCase(product.getInternet_join_yn());
        }

        if ("BRANCH".equals(preferredChannel)) {
            return "Y".equalsIgnoreCase(product.getBranch_join_yn());
        }

        return true;
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