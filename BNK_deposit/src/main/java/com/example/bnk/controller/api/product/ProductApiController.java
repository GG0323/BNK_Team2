package com.example.bnk.controller.api.product;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.product.ProductViewService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductViewService productViewService;
    private final BankMemberService bankMemberService;

    // 공통 성공 응답
    private Map<String, Object> success(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        return body;
    }

    // 공통 실패 응답
    private Map<String, Object> fail(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }

    // 비회원/공통 상품 목록 API
    // 예: /api/products?sort=baseRateDesc&productType=ALL
    @GetMapping
    public Map<String, Object> productList(
            @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort,
            @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType) {

        List<ProductListViewDto> productList =
                productViewService.getProductList(sort, productType);

        return success(productList);
    }

    // 비회원/공통 상품 검색 API
    // 예: /api/products/search?keyword=적금&sort=maxRateDesc&productType=SAVINGS
    @GetMapping("/search")
    public Map<String, Object> searchProductList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort,
            @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType) {

        List<ProductListViewDto> productList =
                productViewService.searchProductList(keyword, sort, productType);

        return success(productList);
    }

    // 로그인 회원 맞춤 상품 목록 API
    // 예: /api/products/member?sort=baseRateDesc&productType=DEPOSIT
    @GetMapping("/member")
    public ResponseEntity<Map<String, Object>> memberProductList(
            Principal principal,
            @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort,
            @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(fail("로그인이 필요합니다."));
        }

        BankMemberDto member =
                bankMemberService.getMemberInfo(principal.getName());

        if (member == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(fail("회원 정보를 찾을 수 없습니다."));
        }

        List<ProductListViewDto> productList =
                productViewService.getProductListForMember(
                        member.getMember_type(),
                        sort,
                        productType
                );

        return ResponseEntity.ok(success(productList));
    }

    // 로그인 회원 맞춤 상품 검색 API
    // 예: /api/products/member/search?keyword=예금&sort=nameAsc&productType=DEPOSIT
    @GetMapping("/member/search")
    public ResponseEntity<Map<String, Object>> memberProductSearch(
            Principal principal,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort,
            @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(fail("로그인이 필요합니다."));
        }

        BankMemberDto member =
                bankMemberService.getMemberInfo(principal.getName());

        if (member == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(fail("회원 정보를 찾을 수 없습니다."));
        }

        List<ProductListViewDto> productList =
                productViewService.searchProductListForMember(
                        member.getMember_type(),
                        keyword,
                        sort,
                        productType
                );

        return ResponseEntity.ok(success(productList));
    }

    // 로그인 회원이 특정 상품 상세 접근 가능한지 확인
    @GetMapping("/member/detail")
    public ResponseEntity<Map<String, Object>> memberProductDetailCheck(
            Principal principal,
            @RequestParam("product_no") long product_no) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(fail("로그인이 필요합니다."));
        }

        BankMemberDto member =
                bankMemberService.getMemberInfo(principal.getName());

        if (member == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(fail("회원 정보를 찾을 수 없습니다."));
        }

        ProductDetailViewDto product =
                productViewService.getProductDetail(product_no);

        if (product == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(fail("상품 정보를 찾을 수 없습니다."));
        }

        String memberType = member.getMember_type();
        String customerType = product.getCustomer_type();

        boolean joinAvailable =
                "ALL".equals(customerType)
                        || memberType.equals(customerType);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("product", product);
        data.put("joinAvailable", joinAvailable);
        data.put("memberType", memberType);
        data.put("message", joinAvailable
                ? "가입 가능한 상품입니다."
                : "해당 회원 유형은 이 상품에 가입할 수 없습니다.");

        return ResponseEntity.ok(success(data));
    }

    // QR 이미지 생성 API
    // 예: /api/products/mobile-qr-image?product_no=1
    @GetMapping(value = "/mobile-qr-image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] productMobileQrImage(
            @RequestParam("product_no") long product_no) throws Exception {

        String qrContent =
                "bnkapp://product/join?product_no=" + product_no;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                220,
                220
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                bitMatrix,
                "PNG",
                outputStream
        );

        return outputStream.toByteArray();
    }
}