package com.example.bnk.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bnk.dto.product.ProductDetailViewDto;
import com.example.bnk.service.product.ProductViewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mobile")
@RequiredArgsConstructor
public class MobileAppOpenPageController {

    private static final String MOBILE_DEEP_LINK_PREFIX = "bnkapp://product/join?product_no=";

    private final ProductViewService productViewService;
    // SG 
    // QR 스캔 후 모바일 안내 페이지
    // 예: /mobile/app-open?product_no=1
    @GetMapping("/app-open")
    public String appOpen(@RequestParam(value = "product_no", required = false) Long product_no,
                          Model model,
                          RedirectAttributes rttr) {

        if (product_no == null || product_no <= 0) {
            rttr.addFlashAttribute("msg", "잘못된 상품 번호입니다.");
            return "redirect:/products";
        }

        ProductDetailViewDto product = productViewService.getProductDetail(product_no);

        if (product == null) {
            rttr.addFlashAttribute("msg", "상품 정보를 찾을 수 없습니다.");
            return "redirect:/products";
        }

        String appDeepLink = MOBILE_DEEP_LINK_PREFIX + product_no;

        model.addAttribute("product", product);
        model.addAttribute("productNo", product_no);
        model.addAttribute("appDeepLink", appDeepLink);

        return "mobile/appOpen";
    }
}