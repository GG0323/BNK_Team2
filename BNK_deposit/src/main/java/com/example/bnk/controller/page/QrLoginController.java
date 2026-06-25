package com.example.bnk.controller.page;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bnk.dao.member.IQrLoginDao;
import com.example.bnk.dto.member.QrLoginMemberDto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QrLoginController {

    private final IQrLoginDao qrLoginDao;

    @Value("${spring.public-base-url:https://192.168.0.87:8443}")
    private String springPublicBaseUrl;

    // QR 인증 상태 저장용 Map
    // key: authId
    // value: 인증 완료 여부
    private Map<String, Boolean> qrLoginStatusMap = new HashMap<>();

    // QR 인증 완료 회원 저장용 Map
    // key: authId
    // value: 회원 정보
    private Map<String, QrLoginMemberDto> qrLoginMemberMap = new HashMap<>();

    // QR 로그인 화면
    @GetMapping("/qr-login")
    public String qrLoginPage(Model model) {

        // PC 화면 접속 시 고유 인증번호 생성
        String authId = UUID.randomUUID().toString();

        // 처음에는 인증 안 된 상태
        qrLoginStatusMap.put(authId, false);

        model.addAttribute("authId", authId);

        return "member/qrLogin";
    }

    // QR 이미지 생성
    @GetMapping(value = "/qr-image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] qrImage(@RequestParam("authId") String authId) throws Exception {

        // 실제 서비스라면 모바일 앱 인증 화면 주소가 들어감
        // 현재는 웹 인증 화면으로 연결
    	String qrContent = springPublicBaseUrl + "/qr-auth?authId=" + authId;

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

    // 모바일에서 QR을 스캔했을 때 보이는 인증 화면
    @GetMapping("/qr-auth")
    public String qrAuthPage(@RequestParam("authId") String authId,
                             Model model) {

        model.addAttribute("authId", authId);

        // 실제 앱에서는 앱 로그인 사용자의 member_no가 자동으로 넘어온다고 가정
        // 지금은 시연용으로 1번 회원 사용
        model.addAttribute("member_no", 1);

        return "member/qrAuth";
    }

    // 모바일 인증 처리
    @PostMapping("/qr-auth")
    public String qrAuthProcess(@RequestParam("authId") String authId,
                                @RequestParam("member_no") long member_no,
                                Model model) {

        QrLoginMemberDto member = qrLoginDao.selectQrLoginMember(member_no);

        if (member == null) {
            model.addAttribute("message", "회원 정보를 찾을 수 없습니다.");
            return "member/qrAuth";
        }

        // 인증 완료 처리
        qrLoginStatusMap.put(authId, true);
        qrLoginMemberMap.put(authId, member);

        model.addAttribute("member", member);

        return "member/qrSuccess";
    }

    // PC 화면에서 QR 인증 완료 여부 확인
    @GetMapping("/qr-status")
    @ResponseBody
    public Map<String, Object> qrStatus(@RequestParam("authId") String authId,
                                        HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        Boolean status = qrLoginStatusMap.get(authId);

        if (status != null && status == true) {

            QrLoginMemberDto member = qrLoginMemberMap.get(authId);

            if (member != null) {
                // 웹 세션 로그인 처리
                session.setAttribute("loginMemberNo", member.getMember_no());
                session.setAttribute("loginId", member.getLogin_id());
                session.setAttribute("loginMemberName", member.getMember_name());
                session.setAttribute("memberType", member.getMember_type());

                result.put("login", true);
                result.put("memberName", member.getMember_name());

                // 인증 끝난 데이터 정리
                qrLoginStatusMap.remove(authId);
                qrLoginMemberMap.remove(authId);

                return result;
            }
        }

        result.put("login", false);

        return result;
    }

    // PC 로그인 완료 화면
    @GetMapping("/qr-login-success")
    public String qrLoginSuccess(HttpSession session,
                                 Model model) {

        Object loginMemberName = session.getAttribute("loginMemberName");

        model.addAttribute("loginMemberName", loginMemberName);

        return "member/qrSuccess";
    }
}
