package com.example.bnk.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesCryptoUtil {

	private static final String CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
	private static final int CBC_IV_LENGTH = 16;
	private static final int GCM_NONCE_LENGTH = 12;
	private static final int GCM_TAG_LENGTH_BIT = 128;
	private static final int GCM_TAG_LENGTH_BYTE = 16;

	@Value("${app.crypto.secret-key}")
	private String secretKey;

	@Value("${app.crypto.gcm-secret-key}")
	private String gcmSecretKey;

	public String encrypt(String plainText) {
		try {
			byte[] keyBytes = keyBytes(secretKey);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

			byte[] iv = new byte[CBC_IV_LENGTH];
			new SecureRandom().nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

			return Base64.getEncoder().encodeToString(combined);
		}catch(Exception e) {
			throw new RuntimeException("AES encryption failed", e);
		}
	}

	public String decrypt(String encryptedText) {
		try {
			byte[] combined = Base64.getDecoder().decode(encryptedText);
			byte[] iv = Arrays.copyOfRange(combined, 0, CBC_IV_LENGTH);
			byte[] encrypted = Arrays.copyOfRange(combined, CBC_IV_LENGTH, combined.length);

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes(secretKey), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted, StandardCharsets.UTF_8);
		}catch(Exception e) {
			throw new RuntimeException("AES decryption failed", e);
		}
	}

	public GcmPayload encryptGcm(byte[] plainBytes) {
		try {
			byte[] nonce = new byte[GCM_NONCE_LENGTH];
			new SecureRandom().nextBytes(nonce);

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes(gcmSecretKey), "AES");
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BIT, nonce);

			Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

			byte[] encryptedWithTag = cipher.doFinal(plainBytes);
			byte[] cipherText = Arrays.copyOfRange(
					encryptedWithTag,
					0,
					encryptedWithTag.length - GCM_TAG_LENGTH_BYTE
			);
			byte[] tag = Arrays.copyOfRange(
					encryptedWithTag,
					encryptedWithTag.length - GCM_TAG_LENGTH_BYTE,
					encryptedWithTag.length
			);

			return new GcmPayload(
					Base64.getEncoder().encodeToString(cipherText),
					Base64.getEncoder().encodeToString(nonce),
					Base64.getEncoder().encodeToString(tag)
			);
		}catch(Exception e) {
			throw new RuntimeException("AES-GCM encryption failed", e);
		}
	}

	public String encryptGcmToString(String plainText) {
		GcmPayload payload = encryptGcm(plainText.getBytes(StandardCharsets.UTF_8));
		return payload.enc() + "." + payload.nonce() + "." + payload.tag();
	}

	public byte[] decryptGcm(GcmPayload payload) {
		return decryptGcm(payload.enc(), payload.nonce(), payload.tag());
	}

	public byte[] decryptGcm(String enc, String nonce, String tag) {
		try {
			byte[] cipherText = Base64.getDecoder().decode(enc);
			byte[] tagBytes = Base64.getDecoder().decode(tag);
			byte[] encryptedWithTag = new byte[cipherText.length + tagBytes.length];
			System.arraycopy(cipherText, 0, encryptedWithTag, 0, cipherText.length);
			System.arraycopy(tagBytes, 0, encryptedWithTag, cipherText.length, tagBytes.length);

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes(gcmSecretKey), "AES");
			GCMParameterSpec gcmSpec = new GCMParameterSpec(
					GCM_TAG_LENGTH_BIT,
					Base64.getDecoder().decode(nonce)
			);

			Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

			return cipher.doFinal(encryptedWithTag);
		}catch(Exception e) {
			throw new RuntimeException("AES-GCM decryption failed", e);
		}
	}

	public String decryptGcmToString(String storedPayload) {
		String[] parts = storedPayload == null ? new String[0] : storedPayload.split("\\.", -1);

		if (parts.length != 3) {
			throw new IllegalArgumentException("AES-GCM payload must be formatted as enc.nonce.tag.");
		}

		return new String(decryptGcm(parts[0], parts[1], parts[2]), StandardCharsets.UTF_8);
	}

	private byte[] keyBytes(String key) {
		byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
		if(keyBytes.length != 32) {
			throw new IllegalArgumentException("AES-256 key must be 32 bytes.");
		}
		return keyBytes;
	}

	public record GcmPayload(String enc, String nonce, String tag) {}
}
