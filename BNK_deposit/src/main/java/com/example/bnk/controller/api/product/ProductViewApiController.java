//package com.example.bnk.controller.api.product;
//
//import java.security.Principal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.bnk.dto.common.ApiResponse;
//import com.example.bnk.dto.member.BankMemberDto;
//import com.example.bnk.dto.product.ProductDetailViewDto;
//import com.example.bnk.dto.product.ProductListViewDto;
//import com.example.bnk.service.member.BankMemberService;
//import com.example.bnk.service.product.ProductViewService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/products")
//@RequiredArgsConstructor
//public class ProductViewApiController {
//
//    private final ProductViewService productViewService;
//    private final BankMemberService bankMemberService;
//
//    // 1. 로그인 회원 유형별 상품 목록 조회
//    @GetMapping("/member")
//    public ResponseEntity<ApiResponse<?>> getProductListForMember(
//            Principal principal,
//            @RequestParam(value = "sort", required = false, defaultValue = "baseRateDesc") String sort) {
//
//        if (principal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.fail("로그인이 필요합니다."));
//        }
//
//        BankMemberDto member = bankMemberService.getMemberInfo(principal.getName());
//
//        if (member == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
//        }
//
//        List<ProductListViewDto> list =
//                productViewService.getProductListForMember(member.getMember_type(), sort);
//
//        return ResponseEntity.ok(ApiResponse.ok(list));
//    }
//
//    // 1-1. 로그인 회원 유형별 상품 검색
//    @GetMapping("/member/search")
//    public ResponseEntity<ApiResponse<?>> searchProductListForMember(
//            Principal principal,
//            @RequestParam(value = "keyword", required = false) String keyword) {
//
//        if (principal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.fail("로그인이 필요합니다."));
//        }
//
//        BankMemberDto member = bankMemberService.getMemberInfo(principal.getName());
//
//        if (member == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
//        }
//
//        List<ProductListViewDto> list =
//                productViewService.searchProductListForMember(member.getMember_type(), keyword);
//
//        return ResponseEntity.ok(ApiResponse.ok(list));
//    }
//
//    // 2. 로그인 회원 기준 상품 상세 조회 + 가입 가능 여부 확인
//    @GetMapping("/member/detail")
//    public ResponseEntity<ApiResponse<?>> getProductDetailForMember(
//            Principal principal,
//            @RequestParam("product_no") long productNo) {
//
//        if (principal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.fail("로그인이 필요합니다."));
//        }
//
//        BankMemberDto member = bankMemberService.getMemberInfo(principal.getName());
//
//        if (member == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
//        }
//
//        ProductDetailViewDto product = productViewService.getProductDetail(productNo);
//
//        if (product == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(ApiResponse.fail("상품 정보를 찾을 수 없습니다."));
//        }
//
//        boolean joinAvailable =
//                "ALL".equals(product.getCustomer_type())
//                || member.getMember_type().equals(product.getCustomer_type());
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("product", product);
//        data.put("joinAvailable", joinAvailable);
//        data.put("memberType", member.getMember_type());
//
//        if (!joinAvailable) {
//            data.put("message", "해당 회원 유형은 이 상품에 가입할 수 없습니다.");
//        }
//
//        return ResponseEntity.ok(ApiResponse.ok(data));
//    }
//}