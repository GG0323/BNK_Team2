package com.example.bnk.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesCryptoUtil {
	
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int IV_LENGTH = 16;
	
	@Value("${app.crypto.secret-key}")
	private String secretKey;
	
	public String encrypt(String plainText) {
		try {
			byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
			
			if (keyBytes.length != 32) {
                throw new IllegalArgumentException("AES-256 키는 32바이트여야 합니다.");
            }
			
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			
			byte[] iv = new byte[IV_LENGTH];
			new java.security.SecureRandom().nextBytes(keyBytes);
			
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			
			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			
			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
			
			return Base64.getEncoder().encodeToString(combined);
			
		}catch(Exception e) {
			throw new RuntimeException("AES 암호화 실패", e);
		}
	}
	
	public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("AES-256 키는 32바이트여야 합니다.");
            }

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("AES 복호화 실패", e);
        }
    }
}
