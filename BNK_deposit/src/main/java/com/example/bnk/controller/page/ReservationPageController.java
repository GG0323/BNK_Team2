package com.example.bnk.controller.page;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bnk.service.member.BankMemberLogService;

import lombok.RequiredArgsConstructor;

/**
 * 영업점 방문 예약 "화면(View) 이동"만 담당하는 컨트롤러.
 *
 * 완전 분리 원칙:
 *  - 이 컨트롤러는 어떤 데이터도 조회하지 않는다. (Service / DAO 의존성 없음, 로그 제외)
 *  - 비어 있는 Thymeleaf 템플릿 경로만 반환한다.
 *  - 영업점 목록·상품명 등 화면 데이터는 페이지 로드 후 JS 가
 *    ReservationApiController(@RestController)를 fetch 로 호출해 JSON 으로 받아 그린다.
 *  - 덕분에 동일한 API 를 웹 브라우저와 모바일 앱이 모두 재사용할 수 있다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class ReservationPageController {

    private final BankMemberLogService logService;
    
    @Value("${kakao.map.js-key}")
    private String kakaoMapKey;

    // 영업점 방문 예약 화면 (껍데기만 반환)
    @GetMapping("/reservation")
    public String rootMembersReservation(
            @AuthenticationPrincipal String username,
            Model model
            ) {
        // 비로그인 차단: username 이 null 이거나 "anonymousUser" 면 로그인 화면으로
        if (username == null || "anonymousUser".equals(username)) {
            return "redirect:/loginPage";
        }

        // 접속 로그 (로그인 사용자만)
        logService.build(logService.findByUserID(username), "member/reservation");
        
        // 카카오맵 키를 화면으로 전달
        model.addAttribute("kakaoMapKey", kakaoMapKey);

        return "member/reservation";
    }
}