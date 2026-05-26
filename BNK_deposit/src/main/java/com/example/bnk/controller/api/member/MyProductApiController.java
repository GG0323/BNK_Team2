package com.example.bnk.controller.api.member;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.member.MyProductDto;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.member.MyProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/myproducts")
@RequiredArgsConstructor
public class MyProductApiController {

    private final MyProductService myProductService;
    private final BankMemberService bankMemberService;

    // 로그인 회원의 가입상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyProducts(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        BankMemberDto member = bankMemberService.getMemberInfo(principal.getName());

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        List<MyProductDto> list = myProductService.getMyProducts(member.getMember_no());

        return ResponseEntity.ok(ApiResponse.ok("조회 성공", list));
    }

    // 로그인 회원의 특정 가입상품 상세 조회
    @GetMapping("/{subscriptionNo}")
    public ResponseEntity<ApiResponse<?>> getMyProductDetail(
            Principal principal,
            @PathVariable("subscriptionNo") int subscriptionNo) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인이 필요합니다."));
        }

        BankMemberDto member = bankMemberService.getMemberInfo(principal.getName());

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        MyProductDto detail =
                myProductService.getMyProductDetail(member.getMember_no(), subscriptionNo);

        if (detail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("가입상품 정보를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.ok("조회 성공", detail));
    }
}