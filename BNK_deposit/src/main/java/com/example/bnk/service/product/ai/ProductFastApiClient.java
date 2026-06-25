package com.example.bnk.service.product.ai;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendRequestDto;
import com.example.bnk.dto.product.ai.ProductPersonaRecommendResponseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProductFastApiClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 90000;
    private static final int LOG_BODY_PREVIEW_LENGTH = 2500;

    private final ObjectMapper objectMapper;

    @Value("${fastapi.base-url:http://127.0.0.1:8000}")
    private String fastApiBaseUrl;

    public ProductFastApiClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ProductPersonaRecommendResponseDto createRecommend(
            ProductPersonaRecommendRequestDto request,
            List<ProductDetailViewDto> products
    ) {
        if (request == null || products == null || products.isEmpty()) {
            return null;
        }

        Map<String, Object> body = toRecommendRequestMap(request);

        List<Map<String, Object>> productPayloads = new ArrayList<>();

        for (ProductDetailViewDto product : products) {
            if (product == null) {
                continue;
            }

            productPayloads.add(toSnakeCaseProductMap(product));
        }

        if (productPayloads.isEmpty()) {
            return null;
        }

        body.put("products", productPayloads);

        JsonNode response = postJson("/fast/api/ai/2/product/recommend", body);

        if (response == null) {
            return null;
        }

        try {
            ProductPersonaRecommendResponseDto result =
                    objectMapper.treeToValue(response, ProductPersonaRecommendResponseDto.class);

            if (result == null
                    || result.getRecommendedProducts() == null
                    || result.getRecommendedProducts().isEmpty()) {
                return null;
            }

            return result;

        } catch (Exception e) {
            System.out.println("[상품 AI] FastAPI 추천 응답 변환 실패. message=" + e.getMessage());
            return null;
        }
    }

    public String createCompareSummary(List<ProductCompareViewDto> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> productPayloads = new ArrayList<>();

        for (ProductCompareViewDto product : products) {
            if (product == null) {
                continue;
            }

            productPayloads.add(toSnakeCaseProductMap(product));
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

        return answer.trim();
    }

    public String createProductSummary(ProductCompareViewDto product) {
        if (product == null) {
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("product", toSnakeCaseProductMap(product));

        JsonNode response = postJson("/fast/api/ai/2/product/summary", body);

        if (response == null) {
            return null;
        }

        String answer = getText(response, "answer");

        if (answer == null || answer.trim().isEmpty()) {
            return null;
        }

        return answer.trim();
    }

    private Map<String, Object> toRecommendRequestMap(ProductPersonaRecommendRequestDto request) {
        Map<String, Object> body = new LinkedHashMap<>();

        putIfNotNull(body, "age", readValue(request, "age"));
        putIfNotNull(body, "balance", readValue(request, "balance"));
        putIfNotNull(body, "monthlyAmount", readValue(request, "monthlyAmount"));
        putIfNotNull(body, "periodMonths", readValue(request, "periodMonths"));

        Object purpose = readValue(request, "purposeSafe", "purpose");
        Object preferredProductType = readValue(request, "preferredProductTypeSafe", "preferredProductType");
        Object preferredChannel = readValue(request, "preferredChannelSafe", "preferredChannel");
        Object interestConditions = readValue(request, "interestConditionsSafe", "interestConditions");

        body.put("purpose", purpose == null ? "MAKE_MONEY" : safeValue(purpose));
        body.put("preferredProductType", preferredProductType == null ? "ALL" : safeValue(preferredProductType));
        body.put("preferredChannel", preferredChannel == null ? "ALL" : safeValue(preferredChannel));
        body.put("interestConditions", interestConditions == null ? List.of() : safeValue(interestConditions));

        return body;
    }

    private Map<String, Object> toSnakeCaseProductMap(Object productDto) {
        Map<String, Object> map = new LinkedHashMap<>();

        putProductValue(map, productDto, "product_no", "product_no", "productNo");
        putProductValue(map, productDto, "product_name", "product_name", "productName");
        putProductValue(map, productDto, "product_type", "product_type", "productType");

        putProductValue(map, productDto, "subtitle", "subtitle");
        putProductValue(map, productDto, "content", "content");

        putProductValue(map, productDto, "min_interest_rate", "min_interest_rate", "minInterestRate");
        putProductValue(map, productDto, "max_interest_rate", "max_interest_rate", "maxInterestRate");

        putProductValue(map, productDto, "interest_payment_type", "interest_payment_type", "interestPaymentType");
        putProductValue(map, productDto, "interest_calc_type", "interest_calc_type", "interestCalcType");

        putProductValue(map, productDto, "branch_join_yn", "branch_join_yn", "branchJoinYn");
        putProductValue(map, productDto, "internet_join_yn", "internet_join_yn", "internetJoinYn");
        putProductValue(map, productDto, "mobile_join_yn", "mobile_join_yn", "mobileJoinYn");

        putProductValue(map, productDto, "join_method_desc", "join_method_desc", "joinMethodDesc");

        putProductValue(map, productDto, "min_join_amount", "min_join_amount", "minJoinAmount");
        putProductValue(map, productDto, "max_join_amount", "max_join_amount", "maxJoinAmount");
        putProductValue(map, productDto, "deposit_unit", "deposit_unit", "depositUnit");

        putProductValue(map, productDto, "min_term_months", "min_term_months", "minTermMonths");
        putProductValue(map, productDto, "max_term_months", "max_term_months", "maxTermMonths");
        putProductValue(map, productDto, "fixed_term_yn", "fixed_term_yn", "fixedTermYn");
        putProductValue(map, productDto, "fixed_term_values", "fixed_term_values", "fixedTermValues");

        putProductValue(map, productDto, "condition_note", "condition_note", "conditionNote");
        putProductValue(map, productDto, "depositor_protection_yn", "depositor_protection_yn", "depositorProtectionYn");

        putProductValue(map, productDto, "preferential_rate_summary", "preferential_rate_summary", "preferentialRateSummary");

        putProductValue(map, productDto, "maturity_rate_label", "maturity_rate_label", "maturityRateLabel");
        putProductValue(map, productDto, "maturity_annual_rate", "maturity_annual_rate", "maturityAnnualRate");
        putProductValue(map, productDto, "maturity_return_rate", "maturity_return_rate", "maturityReturnRate");

        putProductValue(map, productDto, "after_maturity_rate_label", "after_maturity_rate_label", "afterMaturityRateLabel");
        putProductValue(map, productDto, "after_maturity_annual_rate", "after_maturity_annual_rate", "afterMaturityAnnualRate");

        putProductValue(map, productDto, "early_rate_label", "early_rate_label", "earlyRateLabel");
        putProductValue(map, productDto, "early_annual_rate", "early_annual_rate", "earlyAnnualRate");

        return map;
    }

    private void putProductValue(
            Map<String, Object> target,
            Object source,
            String targetKey,
            String... sourceKeys
    ) {
        Object value = readValue(source, sourceKeys);

        if (value == null) {
            return;
        }

        target.put(targetKey, safeValue(value));
    }

    private void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value == null) {
            return;
        }

        target.put(key, safeValue(value));
    }

    private Object readValue(Object source, String... names) {
        if (source == null || names == null) {
            return null;
        }

        for (String name : names) {
            Object value = readByMethod(source, name);

            if (value != null) {
                return value;
            }

            value = readByField(source, name);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private Object readByMethod(Object source, String name) {
        List<String> methodNames = new ArrayList<>();

        methodNames.add(name);
        methodNames.add("get" + capitalize(name));
        methodNames.add("is" + capitalize(name));

        String camelName = toCamelCase(name);

        methodNames.add("get" + capitalize(camelName));
        methodNames.add("is" + capitalize(camelName));

        for (String methodName : methodNames) {
            try {
                Method method = source.getClass().getMethod(methodName);

                if (method.getParameterCount() == 0) {
                    return method.invoke(source);
                }

            } catch (Exception ignored) {
                // 다음 후보 getter 확인
            }
        }

        return null;
    }

    private Object readByField(Object source, String name) {
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add(name);
        fieldNames.add(toCamelCase(name));

        Class<?> currentClass = source.getClass();

        while (currentClass != null && currentClass != Object.class) {
            for (String fieldName : fieldNames) {
                try {
                    Field field = currentClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(source);

                } catch (Exception ignored) {
                    // 다음 후보 field 확인
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    private Object safeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate
                || value instanceof LocalDateTime
                || value instanceof LocalTime
                || value instanceof TemporalAccessor) {
            return value.toString();
        }

        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }

        if (value instanceof List<?>) {
            List<Object> result = new ArrayList<>();

            for (Object item : (List<?>) value) {
                result.add(safeValue(item));
            }

            return result;
        }

        if (value instanceof Map<?, ?>) {
            Map<String, Object> result = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }

                result.put(String.valueOf(entry.getKey()), safeValue(entry.getValue()));
            }

            return result;
        }

        return value;
    }

    private JsonNode postJson(String path, Map<String, Object> body) {
        HttpURLConnection connection = null;

        try {
            String requestJson = objectMapper.writeValueAsString(body);
            byte[] requestBytes = requestJson.getBytes(StandardCharsets.UTF_8);

            String requestUrl = normalizeBaseUrl(fastApiBaseUrl) + path;

            System.out.println("[상품 AI] FastAPI 요청 URL = " + requestUrl);
            System.out.println("[상품 AI] FastAPI 요청 상품 후보 수 = " + getProductPayloadCount(body));
            System.out.println("[상품 AI] FastAPI 요청 JSON 미리보기 = " + preview(requestJson));

            connection = (HttpURLConnection) URI.create(requestUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
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
            System.out.println("[상품 AI] FastAPI 응답 body = " + preview(responseBody));

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

    private int getProductPayloadCount(Map<String, Object> body) {
        if (body == null) {
            return 0;
        }

        Object products = body.get("products");

        if (products instanceof List<?>) {
            return ((List<?>) products).size();
        }

        return 0;
    }

    private String preview(String text) {
        if (text == null) {
            return "";
        }

        if (text.length() <= LOG_BODY_PREVIEW_LENGTH) {
            return text;
        }

        return text.substring(0, LOG_BODY_PREVIEW_LENGTH)
                + "...(생략, totalLength="
                + text.length()
                + ")";
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

    private String getText(JsonNode node, String fieldName) {
        if (node == null) {
            return "";
        }

        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return "";
        }

        return value.asText("");
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String toCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;

        for (char ch : text.toCharArray()) {
            if (ch == '_') {
                upperNext = true;
                continue;
            }

            if (upperNext) {
                builder.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                builder.append(ch);
            }
        }

        return builder.toString();
    }
}