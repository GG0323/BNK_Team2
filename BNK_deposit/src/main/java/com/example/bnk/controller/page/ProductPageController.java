package com.example.bnk.controller.page;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bnk.dto.product.ProductCompareViewDto;
import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.dto.product.ProductListViewDto;
import com.example.bnk.service.product.ProductViewService;
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

    // 상품 목록 조회
    // 비회원도 접근 가능
    // 조건: product_type = DEPOSIT / SAVINGS, product_status = SALE
    @GetMapping
    public String productList(Model model) {

        List<ProductListViewDto> productList = productViewService.getProductList();

        model.addAttribute("productList", productList);

        return "product/productList";
    }

    // 상품 검색
    // TB_KEYWORD.normalized_keyword 활용
    @GetMapping("/search")
    public String searchProductList(@RequestParam(value = "keyword", required = false) String keyword,
                                    Model model) {

        List<ProductListViewDto> productList = productViewService.searchProductList(keyword);

        model.addAttribute("productList", productList);
        model.addAttribute("keyword", keyword);

        return "product/productList";
    }

    // 상품 상세 조회
    // TB_PRODUCT + TB_PRODUCT_DESCRIPTION + TB_PRODUCT_RATE + TB_PRODUCT_CONDITION
    @GetMapping("/detail")
    public String productDetail(@RequestParam("product_no") long product_no,
                                Model model) {

        ProductDetailViewDto product = productViewService.getProductDetail(product_no);

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

        return "product/productCompare";
    }

    // 모바일 상품 가입 QR 안내 페이지
    // 예: /products/mobile-qr?product_no=1
    @GetMapping("/mobile-qr")
    public String productMobileQr(@RequestParam("product_no") long product_no,
                                  Model model) {

        ProductDetailViewDto product = productViewService.getProductDetail(product_no);

        model.addAttribute("product", product);

        return "product/productMobileQr";
    }

    // 모바일 상품 가입 QR 이미지 생성
    // 예: /products/mobile-qr-image?product_no=1
    @GetMapping(value = "/mobile-qr-image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] productMobileQrImage(@RequestParam("product_no") long product_no) throws Exception {

        // 실제 앱 연동 전 임시 앱 이동 주소
        // 나중에 실제 앱 딥링크 주소로 바꾸면 됨
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
	 // 사용자 정보 기반 맞춤 상품 추천
	 // 현재는 시연용으로 member_no = 1 회원 기준 추천
	 @GetMapping("/recommend")
	 public String recommendProductList(Model model) {
	
	     List<ProductListViewDto> productList = productViewService.getRecommendProductList();
	
	     model.addAttribute("productList", productList);
	     model.addAttribute("recommendMode", true);
	
	     return "product/productList";
	 }
    
}