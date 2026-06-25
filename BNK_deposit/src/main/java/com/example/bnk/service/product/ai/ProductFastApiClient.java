package com.example.bnk.service.product.ai;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendItemDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendRequestDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductFastApiClient {

    private final ObjectMapper objectMapper;
    private final String fastApiBaseUrl;

    public ProductFastApiClient(
            @Value("${bnk.fast-api.base-url:http://127.0.0.1:8000}") String fastApiBaseUrl) {

        this.objectMapper = new ObjectMapper();
        this.fastApiBaseUrl = normalizeBaseUrl(fastApiBaseUrl);
    }

    public String createProductSummary(ProductCompareViewDto product) {
        if (product == null) {
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("product", toProductMap(product));

        JsonNode response = postJson("/fast/api/ai/2/product/summary", body);

        if (response == null) {
            return null;
        }

        String answer = getText(response, "answer");

        if (answer == null || answer.trim().isEmpty()) {
            return null;
        }

        return answer;
    }

    public String createCompareSummary(List<ProductCompareViewDto> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> productPayloads = new ArrayList<>();

        for (ProductCompareViewDto product : products) {
            if (product != null) {
                productPayloads.add(toProductMap(product));
            }
        }

        if (productPayloads.isEmpty()) {
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("products", productPayloads);

        JsonNode response = postJson("/fast/api/ai/2/product/compare", body);

        if (response == null) {
            return null;
        }

        String answer = getText(response, "answer");

        if (answer == null || answer.trim().isEmpty()) {
            return null;
        }

        return answer;
    }

    public ProductPersonaRecommendResponseDto recommend(
            ProductPersonaRecommendRequestDto request,
            List<ProductDetailViewDto> products) {

        if (request == null || products == null || products.isEmpty()) {
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("age", request.getAge());
        body.put("balance", request.getBalance());
        body.put("monthlyAmount", request.getMonthlyAmount());
        body.put("periodMonths", request.getPeriodMonths());
        body.put("purpose", request.getPurposeSafe());
        body.put("preferredProductType", request.getPreferredProductTypeSafe());
        body.put("preferredChannel", request.getPreferredChannelSafe());
        body.put("interestConditions", request.getInterestConditionsSafe());

        List<Map<String, Object>> productPayloads = new ArrayList<>();

        for (ProductDetailViewDto product : products) {
            if (product != null) {
                productPayloads.add(toProductMap(product));
            }
        }

        if (productPayloads.isEmpty()) {
            return null;
        }

        body.put("products", productPayloads);

        JsonNode response = postJson("/fast/api/ai/2/product/recommend", body);

        if (response == null) {
            return null;
        }

        return toRecommendResponse(response);
    }

    private JsonNode postJson(String path, Map<String, Object> body) {
        HttpURLConnection connection = null;

        try {
            String requestJson = objectMapper.writeValueAsString(body);
            byte[] requestBytes = requestJson.getBytes(StandardCharsets.UTF_8);

            String requestUrl = fastApiBaseUrl + path;

            System.out.println("[상품 AI] FastAPI 요청 URL = " + requestUrl);
            System.out.println("[상품 AI] FastAPI 요청 JSON = " + requestJson);

            connection = (HttpURLConnection) URI.create(requestUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setFixedLengthStreamingMode(requestBytes.length);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBytes);
                outputStream.flush();
            }

            int statusCode = connection.getResponseCode();

            InputStream responseStream;

            if (statusCode >= 200 && statusCode < 300) {
                responseStream = connection.getInputStream();
            } else {
                responseStream = connection.getErrorStream();
            }

            String responseBody = readResponseBody(responseStream);

            System.out.println("[상품 AI] FastAPI 응답 status = " + statusCode);
            System.out.println("[상품 AI] FastAPI 응답 body = " + responseBody);

            if (statusCode < 200 || statusCode >= 300) {
                return null;
            }

            if (responseBody == null || responseBody.trim().isEmpty()) {
                return null;
            }

            return objectMapper.readTree(responseBody);

        } catch (Exception e) {
            System.out.println("FastAPI 상품 AI 호출 실패. exception="
                    + e.getClass().getName()
                    + ", message="
                    + e.getMessage());
            e.printStackTrace();
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponseBody(InputStream responseStream) throws Exception {
        if (responseStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        return builder.toString();
    }

    private ProductPersonaRecommendResponseDto toRecommendResponse(JsonNode response) {
        ProductPersonaRecommendResponseDto result = new ProductPersonaRecommendResponseDto();
        result.setSummary(getText(response, "summary"));

        JsonNode recommendedProductsNode = response.path("recommendedProducts");

        if (!recommendedProductsNode.isArray()) {
            return result;
        }

        List<ProductPersonaRecommendItemDto> items = new ArrayList<>();

        for (JsonNode itemNode : recommendedProductsNode) {
            ProductPersonaRecommendItemDto item = new ProductPersonaRecommendItemDto();

            item.setProductNo(getLong(itemNode, "productNo"));
            item.setProductName(getText(itemNode, "productName"));
            item.setProductType(getText(itemNode, "productType"));
            item.setSubtitle(getText(itemNode, "subtitle"));

            item.setMinInterestRate(getDouble(itemNode, "minInterestRate"));
            item.setMaxInterestRate(getDouble(itemNode, "maxInterestRate"));

            item.setMinJoinAmount(getLong(itemNode, "minJoinAmount"));
            item.setMaxJoinAmount(getLong(itemNode, "maxJoinAmount"));

            item.setBranchJoinYn(getText(itemNode, "branchJoinYn"));
            item.setInternetJoinYn(getText(itemNode, "internetJoinYn"));
            item.setMobileJoinYn(getText(itemNode, "mobileJoinYn"));

            item.setScore(getInt(itemNode, "score"));
            item.setFitPercent(getInt(itemNode, "fitPercent"));
            item.setBenefitChancePercent(getInt(itemNode, "benefitChancePercent"));

            item.setReason(getText(itemNode, "reason"));
            item.setDetailUrl(getText(itemNode, "detailUrl"));

            JsonNode evidenceNode = itemNode.path("evidence");

            if (evidenceNode.isArray()) {
                List<String> evidence = new ArrayList<>();

                for (JsonNode evidenceItem : evidenceNode) {
                    if (!evidenceItem.isNull()) {
                        evidence.add(evidenceItem.asText());
                    }
                }

                item.setEvidence(evidence);
            }

            items.add(item);
        }

        result.setRecommendedProducts(items);

        return result;
    }

    private Map<String, Object> toProductMap(ProductCompareViewDto product) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("product_no", product.getProduct_no());
        map.put("product_name", product.getProduct_name());
        map.put("product_type", product.getProduct_type());

        map.put("min_interest_rate", product.getMin_interest_rate());
        map.put("max_interest_rate", product.getMax_interest_rate());

        map.put("interest_payment_type", product.getInterest_payment_type());
        map.put("interest_calc_type", product.getInterest_calc_type());

        map.put("branch_join_yn", product.getBranch_join_yn());
        map.put("internet_join_yn", product.getInternet_join_yn());
        map.put("mobile_join_yn", product.getMobile_join_yn());

        map.put("join_method_desc", product.getJoin_method_desc());

        map.put("min_join_amount", product.getMin_join_amount());
        map.put("max_join_amount", product.getMax_join_amount());
        map.put("deposit_unit", product.getDeposit_unit());

        map.put("min_term_months", product.getMin_term_months());
        map.put("max_term_months", product.getMax_term_months());

        map.put("fixed_term_yn", product.getFixed_term_yn());
        map.put("fixed_term_values", product.getFixed_term_values());

        map.put("condition_note", product.getCondition_note());
        map.put("depositor_protection_yn", product.getDepositor_protection_yn());

        map.put("maturity_rate_label", product.getMaturity_rate_label());
        map.put("maturity_annual_rate", product.getMaturity_annual_rate());
        map.put("maturity_return_rate", product.getMaturity_return_rate());

        map.put("after_maturity_rate_label", product.getAfter_maturity_rate_label());
        map.put("after_maturity_annual_rate", product.getAfter_maturity_annual_rate());

        map.put("early_rate_label", product.getEarly_rate_label());
        map.put("early_annual_rate", product.getEarly_annual_rate());

        map.put("detail_url", "/products/detail?product_no=" + product.getProduct_no());

        return map;
    }

    private Map<String, Object> toProductMap(ProductDetailViewDto product) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("product_no", product.getProduct_no());
        map.put("product_name", product.getProduct_name());
        map.put("product_type", product.getProduct_type());

        map.put("subtitle", product.getSubtitle());
        map.put("content", product.getContent());

        map.put("min_interest_rate", product.getMin_interest_rate());
        map.put("max_interest_rate", product.getMax_interest_rate());

        map.put("interest_payment_type", product.getInterest_payment_type());
        map.put("interest_calc_type", product.getInterest_calc_type());

        map.put("branch_join_yn", product.getBranch_join_yn());
        map.put("internet_join_yn", product.getInternet_join_yn());
        map.put("mobile_join_yn", product.getMobile_join_yn());

        map.put("join_method_desc", product.getJoin_method_desc());

        map.put("min_join_amount", product.getMin_join_amount());
        map.put("max_join_amount", product.getMax_join_amount());
        map.put("deposit_unit", product.getDeposit_unit());

        map.put("min_term_months", product.getMin_term_months());
        map.put("max_term_months", product.getMax_term_months());

        map.put("min_age", product.getMin_age());
        map.put("max_age", product.getMax_age());

        map.put("fixed_term_yn", product.getFixed_term_yn());
        map.put("fixed_term_values", product.getFixed_term_values());

        map.put("condition_note", product.getCondition_note());
        map.put("preferential_rate_summary", product.getPreferential_rate_summary());
        map.put("product_feature_desc", product.getProduct_feature_desc());
        map.put("eligibility_desc", product.getEligibility_desc());
        map.put("period_desc", product.getPeriod_desc());
        map.put("amount_desc", product.getAmount_desc());
        map.put("caution_note", product.getCaution_note());

        map.put("depositor_protection_yn", product.getDepositor_protection_yn());

        map.put("detail_url", "/products/detail?product_no=" + product.getProduct_no());

        return map;
    }

    private String getText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return "";
        }

        return value.asText("").trim();
    }

    private long getLong(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return 0L;
        }

        return value.asLong(0L);
    }

    private int getInt(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return 0;
        }

        return value.asInt(0);
    }

    private double getDouble(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return 0.0;
        }

        return value.asDouble(0.0);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "http://127.0.0.1:8000";
        }

        String normalized = baseUrl.trim();

        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}