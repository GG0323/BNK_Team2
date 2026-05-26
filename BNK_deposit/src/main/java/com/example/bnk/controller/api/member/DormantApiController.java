package com.example.bnk.controller.api.member;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.member.EmailService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dormant")
@RequiredArgsConstructor
public class DormantApiController {

    private final BankMemberService bankMemberService;
    private final EmailService emailService;

    private static final String SESSION_DORMANT_LOGIN_ID = "DORMANT_LOGIN_ID";
    private static final String SESSION_DORMANT_EMAIL_CODE = "DORMANT_EMAIL_CODE";
    private static final String SESSION_DORMANT_EMAIL_EXPIRED_AT = "DORMANT_EMAIL_EXPIRED_AT";

    // 휴면 해제 인증번호 발송
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(HttpSession session) {

        String loginId = (String) session.getAttribute(SESSION_DORMANT_LOGIN_ID);

        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("휴면 해제 대상 회원 정보가 없습니다. 다시 로그인해 주세요."));
        }

        BankMemberDto member = bankMemberService.getMemberInfo(loginId);

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("등록된 이메일이 없어 인증번호를 발송할 수 없습니다."));
        }

        String code = createSixDigitCode();

        session.setAttribute(SESSION_DORMANT_EMAIL_CODE, code);
        session.setAttribute(SESSION_DORMANT_EMAIL_EXPIRED_AT, LocalDateTime.now().plusMinutes(5));

        emailService.sendDormantReleaseCode(member.getEmail(), code);

        return ResponseEntity.ok(ApiResponse.success("가입된 이메일로 인증번호를 발송했습니다."));
    }

    // 휴면 해제 인증번호 확인 + 휴면 해제
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> releaseDormant(
            HttpSession session,
            @RequestParam("code") String inputCode) {

        String loginId = (String) session.getAttribute(SESSION_DORMANT_LOGIN_ID);
        String savedCode = (String) session.getAttribute(SESSION_DORMANT_EMAIL_CODE);
        LocalDateTime expiredAt =
                (LocalDateTime) session.getAttribute(SESSION_DORMANT_EMAIL_EXPIRED_AT);

        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("휴면 해제 대상 회원 정보가 없습니다. 다시 로그인해 주세요."));
        }

        if (savedCode == null || expiredAt == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("인증번호를 먼저 발송해 주세요."));
        }

        if (LocalDateTime.now().isAfter(expiredAt)) {
            removeDormantEmailSession(session);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("인증번호가 만료되었습니다. 다시 발송해 주세요."));
        }

        if (!savedCode.equals(inputCode)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("인증번호가 일치하지 않습니다."));
        }

        boolean released = bankMemberService.releaseDormant(loginId);

        if (!released) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }

        bankMemberService.updateLastLoginAt(loginId);

        removeDormantEmailSession(session);
        session.removeAttribute(SESSION_DORMANT_LOGIN_ID);

        return ResponseEntity.ok(ApiResponse.success("휴면 계정이 해제되었습니다. 다시 로그인해 주세요."));
    }

    private String createSixDigitCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private void removeDormantEmailSession(HttpSession session) {
        session.removeAttribute(SESSION_DORMANT_EMAIL_CODE);
        session.removeAttribute(SESSION_DORMANT_EMAIL_EXPIRED_AT);
    }
}