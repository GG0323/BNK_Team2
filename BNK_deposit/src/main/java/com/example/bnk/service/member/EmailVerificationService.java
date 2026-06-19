package com.example.bnk.service.member;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 5;
    private static final int EXPIRATION_MINUTES = 5;

    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Verification> verifications = new ConcurrentHashMap<>();

    public void sendSignupCode(String email) {
        String normalizedEmail = normalize(email);
        String code = createCode();

        verifications.put(normalizedEmail, new Verification(
                code,
                LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES),
                0,
                false
        ));

        emailService.sendSignupVerificationCode(normalizedEmail, code);
    }

    public boolean confirmSignupCode(String email, String code) {
        String normalizedEmail = normalize(email);
        Verification verification = verifications.get(normalizedEmail);

        if (verification == null || verification.isExpired() || verification.attemptCount() >= MAX_ATTEMPTS) {
            verifications.remove(normalizedEmail);
            return false;
        }

        if (!verification.code().equals(code)) {
            verifications.put(normalizedEmail, verification.failedAttempt());
            return false;
        }

        verifications.put(normalizedEmail, verification.markVerified());
        return true;
    }

    public boolean isSignupVerified(String email) {
        String normalizedEmail = normalize(email);
        Verification verification = verifications.get(normalizedEmail);

        if (verification == null || verification.isExpired()) {
            verifications.remove(normalizedEmail);
            return false;
        }

        return verification.verified();
    }

    public void consumeSignupVerification(String email) {
        verifications.remove(normalize(email));
    }

    public String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String createCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }

        return code.toString();
    }

    private record Verification(
            String code,
            LocalDateTime expiresAt,
            int attemptCount,
            boolean verified
    ) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        Verification failedAttempt() {
            return new Verification(code, expiresAt, attemptCount + 1, false);
        }

        Verification markVerified() {
            return new Verification(code, expiresAt, attemptCount, true);
        }
    }
}
