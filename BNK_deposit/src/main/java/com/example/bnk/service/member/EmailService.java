package com.example.bnk.service.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 휴면 해제 인증번호 발송
    public void sendDormantReleaseCode(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[BNK] 휴면 계정 해제 인증번호");
        message.setText(
                "BNK 휴면 계정 해제 인증번호입니다.\n\n"
                + "인증번호: " + code + "\n\n"
                + "인증번호는 5분 동안만 유효합니다."
        );

        mailSender.send(message);
    }
}