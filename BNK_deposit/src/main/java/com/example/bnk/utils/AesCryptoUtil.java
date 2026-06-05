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
	
	// AES 블록 크기에 맞게 평문을 패딩하여 암호화하는 방식
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	
	// AES CBC 모드에서 사용 될 초기화 벡터 길이
	private static final int IV_LENGTH = 16;
	
	// 프로퍼티스에 저장된 AES key 값
	@Value("${app.crypto.secret-key}")
	private String secretKey;
	
	// 암호화
	public String encrypt(String plainText) {
		try {
			// String 타입의 AES key를 바이트로 변환 
			byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
			
			// AES-256의 키 길이는 32로 고정이므로 맞는지 확인
			if (keyBytes.length != 32) {
                throw new IllegalArgumentException("AES-256 키는 32바이트여야 합니다.");
            }
			
			// AES 키 스펙 저장(AES 키 객체 생성)
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			
			// AES CBC 모드에서 사용할 초기화 벡터
			byte[] iv = new byte[IV_LENGTH];
			
			// 동일한 평문도 매번 다르게 암호문이 되도록 초기화 벡터에 랜덤 값 채움
			new java.security.SecureRandom().nextBytes(iv);
			
			// 초기화 벡터를 Cipher에 전달할 수 있는 파라미터 객체로 생성
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			// 암호화/복호화 객체 생성
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			
			// CBC 방식에서 사용할 비밀키와 초기화 벡터를 사용하여 암호화 모드로 초기화
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			
			// 평문을 바이트로 변환한 뒤 암호화 수행
			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			
			// 암호문과 초기화 벡터를 담기위한 바이트 배열 생성
			byte[] combined = new byte[iv.length + encrypted.length];
			
			// combined 배열 앞 16바이트에 초기화 벡터 복사
			System.arraycopy(iv, 0, combined, 0, iv.length);
			
			// combined 배열에서 초기화 벡터 뒤에 암호문 바이트 배열 복사
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
			
			// Base64로 바이트를 문자열로 인코딩
			return Base64.getEncoder().encodeToString(combined);
			
		}catch(Exception e) {
			throw new RuntimeException("AES 암호화 실패", e);
		}
	}
	
	// 복호화
	public String decrypt(String encryptedText) {
        try {
        	
        	// Base64로 문자열로 저장된 암호문을 바이트로 디코딩
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            // combined 앞 16바이트를 초기화 벡터로 분리
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            
            // combined 초기화 벡터 뒷부분을 암호문으로 분리
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
            
            // String 타입의 AES key를 바이트로 변환
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            
            // AES-256 키 길이인 32바이트인지 확인
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("AES-256 키는 32바이트여야 합니다.");
            }
            
            // AES 키 스펙 저장(AES 키 객체 생성)
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            // AES CBC 모드에서 사용할 초기화 벡터
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // 암호화/복호화 객체 생성
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // CBC 방식에서 사용할 비밀키와 초기화 벡터를 사용하여 복호화 모드로 초기화
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // 암호문을 복호화하여 평문 바이트 배열 생성
            byte[] decrypted = cipher.doFinal(encrypted);
            
            // 복호화된 바이트 배열을 문자열로 변환
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("AES 복호화 실패", e);
        }
    }
}
