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
    
    // 회원가입 시 이메일 인증번호 발송
    public void sendSignupVerificationCode(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[BNK] 회원가입 이메일 인증번호");
        message.setText(
                "BNK 회원가입 이메일 인증번호입니다.\n\n"
                + "인증번호: " + code + "\n\n"
                + "인증번호는 5분 동안만 유효합니다."
        );

        mailSender.send(message);
    }
    
    // 보안카드 이메일로 전송
    public void sendSecurityCard(String toEmail, String securityCardNumber, long pk) {

        SimpleMailMessage message = new SimpleMailMessage();
        String [] numbers = securityCardNumber.split(" ");
        int no = 1;

        String msg = "BNK 준회원 가입이 완료되었습니다.\n\n"
        		+ "계좌 개설 시 사용할 보안카드 번호입니다.\n\n"
        		+ "[보안카드 번호]\n"
        		+ "No. " + pk + "\n";
        
        for(int i = 0; i < numbers.length; i+=2) {
        	msg += String.format("%d: %s %s\n", no++, numbers[i], numbers[i+1]);
        }
        
        msg += "\n해당 번호는 타인에게 공유하지 마세요.";
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[BNK] 보안카드 번호 안내");
        message.setText(msg);

        mailSender.send(message);
    }
}