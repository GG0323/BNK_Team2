package com.example.bnk.controller.page;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.product.ProductViewService;
import com.example.bnk.service.product.ai.ProductCompareAiService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductPageController {

    private final ProductViewService productViewService;
    private final BankMemberService bankMemberService;
    private final ProductCompareAiService productCompareAiService;

    // 상품 목록 조회
    // 비회원도 접근 가능
    // 조건: product_type = DEPOSIT / SAVINGS, product_status = SALE
    @GetMapping
    public String productList(Model model,
                              Principal principal,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType,
                              @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort) {

        String normalizedProductType = normalizeProductType(productType);

        List<ProductListViewDto> productList;

        if (keyword != null && !keyword.trim().equals("")) {
            productList = productViewService.searchProductList(keyword, sort, normalizedProductType);
        } else {
            productList = productViewService.getProductList(sort, normalizedProductType);
        }

        model.addAttribute("productList", productList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productType", normalizedProductType);
        model.addAttribute("sort", sort);

        addRecommendationAttributes(model, principal);

        return "product/productList";
    }

    // 상품 검색
    // 기존 /products/search URL 호환용
    // TB_KEYWORD.normalized_keyword 활용
    @GetMapping("/search")
    public String searchProductList(@RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType,
                                    @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort,
                                    Model model,
                                    Principal principal) {

        String normalizedProductType = normalizeProductType(productType);

        List<ProductListViewDto> productList =
                productViewService.searchProductList(keyword, sort, normalizedProductType);

        model.addAttribute("productList", productList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productType", normalizedProductType);
        model.addAttribute("sort", sort);

        addRecommendationAttributes(model, principal);

        return "product/productList";
    }

    // 상품 상세 조회
    // TB_PRODUCT + TB_PRODUCT_DESCRIPTION + TB_PRODUCT_RATE + TB_PRODUCT_CONDITION
    @GetMapping("/detail")
    public String productDetail(@RequestParam("product_no") long product_no,
                                Model model,
                                Principal principal,
                                RedirectAttributes rttr) {

        ProductDetailViewDto product = productViewService.getProductDetail(product_no);

        if (product == null) {
            rttr.addFlashAttribute("msg", "상품 정보를 찾을 수 없습니다.");
            return "redirect:/products";
        }

        // 로그인한 회원이면 회원 유형과 상품 고객 유형 비교
        if (principal != null) {
            BankMemberDto member = bankMemberService.getMemberInfo(principal.getName());

            if (member != null) {
                String memberType = member.getMember_type();
                String customerType = product.getCustomer_type();

                boolean available =
                        "ALL".equals(customerType)
                        || memberType.equals(customerType);

                if (!available) {
                    rttr.addFlashAttribute("msg", "해당 회원 유형은 이 상품에 접근할 수 없습니다.");
                    return "redirect:/products";
                }
            }
        }

        model.addAttribute("product", product);

        return "product/productDetail";
    }

    // 상품 비교
    // 예: /products/compare?ids=1,2,3
    @GetMapping("/compare")
    public String productCompare(@RequestParam(value = "ids", required = false) String ids,
                                 Model model) {

        List<ProductCompareViewDto> compareList = productViewService.getCompareProducts(ids);

        model.addAttribute("compareList", compareList);
        model.addAttribute("ids", ids);

        return "product/productCompare";
    }

    // 상품 비교 AI 요약
    // 예: /products/compare-ai-summary?ids=1,2,3
    @GetMapping(value = "/compare-ai-summary", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String compareAiSummary(@RequestParam(value = "ids", required = false) String ids) {

        List<ProductCompareViewDto> compareList = productViewService.getCompareProducts(ids);

        if (compareList == null || compareList.isEmpty()) {
            return "비교할 상품 정보가 없습니다.";
        }

        return productCompareAiService.createCompareSummary(compareList);
    }

    // 비교 페이지 상품 1개 AI 요약
    // 예: /products/compare-product-ai-summary?ids=1,2,3&product_no=1
    @GetMapping(value = "/compare-product-ai-summary", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String compareProductAiSummary(@RequestParam(value = "ids", required = false) String ids,
                                          @RequestParam("product_no") long product_no) {

        List<ProductCompareViewDto> compareList = productViewService.getCompareProducts(ids);

        if (compareList == null || compareList.isEmpty()) {
            return "요약할 상품 정보가 없습니다.";
        }

        ProductCompareViewDto targetProduct = null;

        for (ProductCompareViewDto product : compareList) {
            if (product.getProduct_no() == product_no) {
                targetProduct = product;
                break;
            }
        }

        if (targetProduct == null) {
            return "요약할 상품을 찾을 수 없습니다.";
        }

        return productCompareAiService.createCompareProductSummary(targetProduct);
    }

    // 모바일 상품 가입 QR 안내 페이지
    // 예: /products/mobile-qr?product_no=1
    @GetMapping("/mobile-qr")
    public String productMobileQr(@RequestParam("product_no") long product_no,
                                  Model model,
                                  RedirectAttributes rttr) {

        ProductDetailViewDto product = productViewService.getProductDetail(product_no);

        if (product == null) {
            rttr.addFlashAttribute("msg", "상품 정보를 찾을 수 없습니다.");
            return "redirect:/products";
        }

        model.addAttribute("product", product);

        return "product/productMobileQr";
    }

    // 모바일 상품 가입 QR 이미지 생성
    // 기존 화면 URL 호환을 위해 유지
    // API 전용 URL은 /api/products/mobile-qr-image 사용 가능
    @GetMapping(value = "/mobile-qr-image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] productMobileQrImage(@RequestParam("product_no") long product_no) throws Exception {

        String qrContent = "bnkapp://product/join?product_no=" + product_no;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                220,
                220
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    // 추천 화면 테스트 URL
    // /products와 같은 화면을 사용하되 추천 영역을 확인하기 위한 용도
    @GetMapping("/recommend")
    public String recommendProductList(Model model,
                                       Principal principal,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "productType", required = false, defaultValue = "ALL") String productType,
                                       @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort) {

        String normalizedProductType = normalizeProductType(productType);

        List<ProductListViewDto> productList;

        if (keyword != null && !keyword.trim().equals("")) {
            productList = productViewService.searchProductList(keyword, sort, normalizedProductType);
        } else {
            productList = productViewService.getProductList(sort, normalizedProductType);
        }

        model.addAttribute("productList", productList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productType", normalizedProductType);
        model.addAttribute("sort", sort);
        model.addAttribute("recommendMode", true);

        addRecommendationAttributes(model, principal);

        return "product/productList";
    }

    private void addRecommendationAttributes(Model model, Principal principal) {
        BankMemberDto member = getLoginMember(principal);

        List<ProductListViewDto> recommendedProducts;
        String recommendationMode;
        String recommendationMessage;

        if (member != null && productViewService.isPersonalRecommendationTarget(member)) {
            recommendedProducts = productViewService.getRecommendedProductsForMember(member);
            recommendationMode = productViewService.getRecommendationMode(member);
            recommendationMessage = productViewService.getRecommendationMessage(member);
        } else {
            recommendedProducts = productViewService.getPopularRecommendedProducts();
            recommendationMode = "POPULAR";
            recommendationMessage = productViewService.getRecommendationMessage(null);
        }

        model.addAttribute("recommendedProducts", recommendedProducts);
        model.addAttribute("recommendationMode", recommendationMode);
        model.addAttribute("recommendationMessage", recommendationMessage);
    }

    private BankMemberDto getLoginMember(Principal principal) {
        if (principal == null) {
            return null;
        }

        return bankMemberService.getMemberInfo(principal.getName());
    }

    private String normalizeProductType(String productType) {
        if ("DEPOSIT".equals(productType)) {
            return "DEPOSIT";
        }

        if ("SAVINGS".equals(productType)) {
            return "SAVINGS";
        }

        return "ALL";
    }
}