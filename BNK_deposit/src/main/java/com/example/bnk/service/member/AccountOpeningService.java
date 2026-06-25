package com.example.bnk.service.member;

import java.nio.charset.StandardCharsets;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dao.member.IAccountOpeningDao;
import com.example.bnk.dao.member.ISecurityCardDao;
import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.AccountOpeningAuthDto;
import com.example.bnk.dto.member.SecurityCardDto;
import com.example.bnk.utils.AesCryptoUtil;
import com.example.bnk.utils.AesCryptoUtil.GcmPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class AccountOpeningService {

	private static final Logger log = LoggerFactory.getLogger(AccountOpeningService.class);

	private static final String FLOW_KEY = "_flow";
	private static final String ID_CARD_SAVE_FAILED = "\uC2E0\uBD84\uC99D \uC774\uBBF8\uC9C0 \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uCD2C\uC601\uD574\uC8FC\uC138\uC694.";
	private static final String ID_CARD_VERIFY_FAILED = "\uC2E0\uBD84\uC99D \uC778\uC99D \uCC98\uB9AC \uC911 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uCD2C\uC601\uD574\uC8FC\uC138\uC694.";
	private static final String FACE_SAVE_FAILED = "\uC5BC\uAD74 \uC774\uBBF8\uC9C0 \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uCD2C\uC601\uD574\uC8FC\uC138\uC694.";
	private static final String FACE_VERIFY_FAILED = "\uC5BC\uAD74 \uC778\uC99D \uCC98\uB9AC \uC911 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uCD2C\uC601\uD574\uC8FC\uC138\uC694.";
	private static final String OCR_SAVE_FAILED = "OCR \uACB0\uACFC \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.";
	private static final String PRIVACY_CONSENT_SAVE_FAILED = "\uAC1C\uC778\uC815\uBCF4 \uB3D9\uC758 \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.";
	private static final String FASTAPI_TIMEOUT_MESSAGE = "\uC778\uC99D \uCC98\uB9AC \uC2DC\uAC04\uC774 \uCD08\uACFC\uD558\uC600\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.";

	private static final String SECURITY_CARD_SIDE_FRONT = "FRONT";
	private static final String SECURITY_CARD_SIDE_BACK = "BACK";

	private static final int SECURITY_CARD_NUMBER_COUNT = 20;
	private static final int SECURITY_CARD_PAIR_COUNT = 10;
	private static final int SECURITY_CARD_PAIR_SIZE = 2;
	private static final int SECURITY_CARD_MAX_FAILURES = 5;

	private static final Set<String> PURPOSE_CODES = Set.of(
			"SALARY",
			"PART_TIME_SALARY",
			"PENSION",
			"BUSINESS",
			"GROUP",
			"UTILITY_PAYMENT",
			"LIVING_EXPENSE",
			"ETC"
	);

	private final IAccountOpeningDao accountOpeningDao;
	private final ISecurityCardDao securityCardDao;
	private final AccountService accountService;
	private final AesCryptoUtil aesCryptoUtil;
	@Qualifier("fastApiRestTemplate")
	private final RestTemplate restTemplate;
	private final PlatformTransactionManager transactionManager;
	private final SecureRandom secureRandom = new SecureRandom();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public AccountOpeningService(
			IAccountOpeningDao accountOpeningDao,
			ISecurityCardDao securityCardDao,
			AccountService accountService,
			AesCryptoUtil aesCryptoUtil,
			@Qualifier("fastApiRestTemplate") RestTemplate restTemplate,
			PlatformTransactionManager transactionManager
	) {
		this.accountOpeningDao = accountOpeningDao;
		this.securityCardDao = securityCardDao;
		this.accountService = accountService;
		this.aesCryptoUtil = aesCryptoUtil;
		this.restTemplate = restTemplate;
		this.transactionManager = transactionManager;
	}

	@Value("${fastapi.base-url:http://192.168.0.87:8000}")
	private String fastApiBaseUrl;

	public Map<String, Object> status(long memberNo) {
		AccountOpeningAuthDto auth = accountOpeningDao.findByMemberNo(memberNo);
		Map<String, Object> flow = readFlow(auth);

		boolean privacyConsent = auth != null && "Y".equalsIgnoreCase(auth.getPrivacyConsentYn());
		boolean idCardUploaded = auth != null && hasText(auth.getIdcardImageEnc());
		boolean ocrReady = auth != null && hasText(auth.getOcrResultEnc());
		boolean ocrConfirmed = bool(flow.get("ocrConfirmed"));
		boolean faceUploaded = auth != null && hasText(auth.getFaceImageEnc());
		boolean faceVerified = bool(flow.get("faceVerified"));
		boolean securityVerified = bool(flow.get("securityCardVerified"));
		boolean accountConsent = bool(flow.get("accountConsentAgreed"));
		boolean passwordSet = hasText((String) flow.get("accountPassword"));
		boolean purposeSet = hasText((String) flow.get("accountPurpose"));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("privacyConsent", privacyConsent);
		result.put("idCardUploaded", idCardUploaded);
		result.put("ocrReady", ocrReady);
		result.put("ocrConfirmed", ocrConfirmed);
		result.put("faceUploaded", faceUploaded);
		result.put("faceVerified", faceVerified);
		result.put("securityCardVerified", securityVerified);
		result.put("accountConsent", accountConsent);
		result.put("passwordSet", passwordSet);
		result.put("purposeSet", purposeSet);
		result.put("purpose", flow.get("accountPurpose"));
		result.put("readyToOpen", privacyConsent
				&& ocrReady
				&& ocrConfirmed
				&& faceVerified
				&& securityVerified
				&& accountConsent
				&& passwordSet
				&& purposeSet);
		result.put("nextStep", nextStep(result));
		return result;
	}

	public Map<String, Object> savePrivacyConsent(long memberNo, boolean agreed) {
		if (!agreed) {
			throw new IllegalArgumentException("개인정보 수집·이용 동의가 필요합니다.");
		}
		savePrivacyConsentValue(memberNo, "Y");
		return status(memberNo);
	}

	public Map<String, Object> uploadIdCard(long memberNo, MultipartFile image) {
		requireImage(image);
		GcmPayload encrypted = encryptFile(image);
		validateEncryptedPayload("idCard", encrypted);

		log.info(
				"idCard file received: exists=true, size={}, contentType={}",
				image.getSize(),
				image.getContentType()
		);
		log.info(
				"idCard encrypted payload: encExists={}, encLength={}, nonceExists={}, nonceLength={}, tagExists={}, tagLength={}",
				hasText(encrypted.enc()),
				length(encrypted.enc()),
				hasText(encrypted.nonce()),
				length(encrypted.nonce()),
				hasText(encrypted.tag()),
				length(encrypted.tag())
		);

		runRequiresNew(() -> {
			int updated = accountOpeningDao.updateIdCardImage(
					memberNo,
					encrypted.enc(),
					encrypted.nonce(),
					encrypted.tag()
			);
			if (updated == 0) {
				insertBaseRowIfMissing(memberNo);
				updated = accountOpeningDao.updateIdCardImage(
						memberNo,
						encrypted.enc(),
						encrypted.nonce(),
						encrypted.tag()
				);
			}
			if (updated == 0 || accountOpeningDao.existsIdCardImage(memberNo) != 1) {
				if (friendlyMessagesEnabled()) {
					throw new IllegalStateException(ID_CARD_SAVE_FAILED);
				}
				throw new IllegalStateException("신분증 이미지 저장에 실패했습니다. 다시 촬영해주세요.");
			}
		});

		if (accountOpeningDao.existsIdCardImage(memberNo) != 1) {
			if (friendlyMessagesEnabled()) {
				throw new IllegalStateException(ID_CARD_SAVE_FAILED);
			}
			throw new IllegalStateException("신분증 이미지 저장에 실패했습니다. 다시 촬영해주세요.");
		}

		log.info("idCard saved check: exists={}", true);

		boolean result = callFastApi("/fast/api/auth/2/member", memberNo, "ID_CARD_VERIFY_TIMEOUT");
		if (!result) {
			if (friendlyMessagesEnabled()) {
				throw new IllegalStateException(ID_CARD_VERIFY_FAILED);
			}
			throw new IllegalStateException("신분증 인증 처리 중 오류가 발생했습니다. 다시 촬영해주세요.");
		}
		return status(memberNo);
	}

	public Map<String, Object> getOcr(long memberNo) {
		AccountOpeningAuthDto auth = requireAuth(memberNo);
		Map<String, Object> ocr = readOcr(auth);
		Map<String, Object> flow = flowMap(ocr);
		ocr.remove(FLOW_KEY);
		return Map.of(
				"ocr", ocr,
				"confirmed", bool(flow.get("ocrConfirmed"))
		);
	}

	public Map<String, Object> updateOcr(long memberNo, Map<String, Object> ocr) {
		if (ocr == null || ocr.isEmpty()) {
			throw new IllegalArgumentException("OCR 결과가 비어 있습니다.");
		}
		AccountOpeningAuthDto auth = requireAuth(memberNo);
		Map<String, Object> merged = readOcr(auth);
		Map<String, Object> flow = flowMap(merged);
		merged.clear();
		merged.putAll(ocr);
		flow.put("ocrConfirmed", true);
		merged.put(FLOW_KEY, flow);
		writeOcr(memberNo, merged);
		return status(memberNo);
	}

	public Map<String, Object> uploadFace(long memberNo, MultipartFile image) {
		requireImage(image);
		GcmPayload encrypted = encryptFile(image);
		validateEncryptedPayload("face", encrypted);

		runRequiresNew(() -> {
			int updated = accountOpeningDao.updateFaceImage(
					memberNo,
					encrypted.enc(),
					encrypted.nonce(),
					encrypted.tag()
			);
			if (updated == 0) {
				insertBaseRowIfMissing(memberNo);
				updated = accountOpeningDao.updateFaceImage(
						memberNo,
						encrypted.enc(),
						encrypted.nonce(),
						encrypted.tag()
				);
			}
			if (updated == 0 || accountOpeningDao.existsFaceImage(memberNo) != 1) {
				if (friendlyMessagesEnabled()) {
					throw new IllegalStateException(FACE_SAVE_FAILED);
				}
				throw new IllegalStateException("얼굴 이미지 저장에 실패했습니다. 다시 촬영해주세요.");
			}
		});

		if (accountOpeningDao.existsFaceImage(memberNo) != 1) {
			if (friendlyMessagesEnabled()) {
				throw new IllegalStateException(FACE_SAVE_FAILED);
			}
			throw new IllegalStateException("얼굴 이미지 저장에 실패했습니다. 다시 촬영해주세요.");
		}

		boolean result = callFastApi("/fast/api/auth/2/face", memberNo, "FACE_VERIFY_TIMEOUT");
		if (!result) {
			if (friendlyMessagesEnabled()) {
				throw new IllegalStateException(FACE_VERIFY_FAILED);
			}
			throw new IllegalStateException("얼굴 인증 처리 중 오류가 발생했습니다. 다시 촬영해주세요.");
		}

		Map<String, Object> ocr = readOcr(requireAuth(memberNo));
		Map<String, Object> flow = flowMap(ocr);
		flow.put("faceVerified", true);
		ocr.put(FLOW_KEY, flow);
		writeOcr(memberNo, ocr);
		return status(memberNo);
	}

	public Map<String, Object> createSecurityCardChallenge(long memberNo) {
		SecurityCardDto card = requireSecurityCard(memberNo);
		String decryptedSecurityCard = decryptSecurityCardRaw(card.getSec_num());
		List<String> numbers = parseSecurityCardNumbers(decryptedSecurityCard);
		List<List<String>> pairs = toSecurityCardPairs(numbers);

		logSecurityCardPayload(memberNo, card.getSec_num(), decryptedSecurityCard, numbers);

		int frontIndex = secureRandom.nextInt(SECURITY_CARD_PAIR_COUNT);
		int backIndex = secureRandom.nextInt(SECURITY_CARD_PAIR_COUNT);

		while (backIndex == frontIndex) {
			backIndex = secureRandom.nextInt(SECURITY_CARD_PAIR_COUNT);
		}

		String frontSide = SECURITY_CARD_SIDE_FRONT;
		String backSide = SECURITY_CARD_SIDE_BACK;

		String expectedFullValue1 = String.join(" ", pairs.get(frontIndex));
		String expectedPart1 = securityCardPairPart(pairs.get(frontIndex), frontSide);

		String expectedFullValue2 = String.join(" ", pairs.get(backIndex));
		String expectedPart2 = securityCardPairPart(pairs.get(backIndex), backSide);

		logSecurityCardChallenge(
				frontIndex,
				frontSide,
				expectedFullValue1,
				expectedPart1,
				backIndex,
				backSide,
				expectedFullValue2,
				expectedPart2
		);

		Map<String, Object> ocr = readOcr(requireAuth(memberNo));
		Map<String, Object> flow = flowMap(ocr);

		flow.put("securityFrontIndex", frontIndex);
		flow.put("securityFrontSide", frontSide);
		flow.put("securityBackIndex", backIndex);
		flow.put("securityBackSide", backSide);
		flow.putIfAbsent("securityFailures", 0);
		flow.put("securityCardVerified", false);

		ocr.put(FLOW_KEY, flow);
		writeOcr(memberNo, ocr);

		return Map.of(
				"frontIndex", frontIndex + 1,
				"backIndex", backIndex + 1,
				"message", (frontIndex + 1) + "번의 앞 번호와 " + (backIndex + 1) + "번의 뒷 번호를 입력해주세요."
		);
	}

	public Map<String, Object> verifySecurityCard(long memberNo, String frontAnswer, String backAnswer) {
		SecurityCardDto card = requireSecurityCard(memberNo);
		String decryptedSecurityCard = decryptSecurityCardRaw(card.getSec_num());
		List<String> numbers = parseSecurityCardNumbers(decryptedSecurityCard);
		List<List<String>> pairs = toSecurityCardPairs(numbers);

		logSecurityCardPayload(memberNo, card.getSec_num(), decryptedSecurityCard, numbers);

		Map<String, Object> ocr = readOcr(requireAuth(memberNo));
		Map<String, Object> flow = flowMap(ocr);

		int failures = intValue(flow.get("securityFailures"));
		if (failures >= SECURITY_CARD_MAX_FAILURES) {
			throw new IllegalStateException("보안카드 인증 실패 횟수를 초과했습니다.");
		}

		Integer frontIndex = nullableIntValue(flow.get("securityFrontIndex"));
		Integer backIndex = nullableIntValue(flow.get("securityBackIndex"));

		validateSecurityCardPairIndex(frontIndex, "front");
		validateSecurityCardPairIndex(backIndex, "back");

		String frontSide = securityCardSide(flow.get("securityFrontSide"), SECURITY_CARD_SIDE_FRONT);
		String backSide = securityCardSide(flow.get("securityBackSide"), SECURITY_CARD_SIDE_BACK);

		String expectedFullValue1 = String.join(" ", pairs.get(frontIndex));
		String expectedPart1 = securityCardPairPart(pairs.get(frontIndex), frontSide);

		String expectedFullValue2 = String.join(" ", pairs.get(backIndex));
		String expectedPart2 = securityCardPairPart(pairs.get(backIndex), backSide);

		String normalizedFrontAnswer = cleanAnswer(frontAnswer);
		String normalizedBackAnswer = cleanAnswer(backAnswer);

		boolean match1 = expectedPart1.equals(normalizedFrontAnswer);
		boolean match2 = expectedPart2.equals(normalizedBackAnswer);

		logSecurityCardVerify(
				frontIndex,
				frontSide,
				normalizedFrontAnswer,
				expectedFullValue1,
				expectedPart1,
				match1,
				backIndex,
				backSide,
				normalizedBackAnswer,
				expectedFullValue2,
				expectedPart2,
				match2
		);

		boolean verified = match1 && match2;

		if (!verified) {
			flow.put("securityFailures", failures + 1);
			ocr.put(FLOW_KEY, flow);
			writeOcr(memberNo, ocr);
			throw new IllegalArgumentException("보안카드 번호가 일치하지 않습니다.");
		}

		flow.put("securityCardVerified", true);
		flow.put("securityFailures", 0);

		ocr.put(FLOW_KEY, flow);
		writeOcr(memberNo, ocr);

		return status(memberNo);
	}

	public Map<String, Object> saveAccountConsent(long memberNo, boolean agreed) {
		if (!agreed) {
			throw new IllegalArgumentException("계좌 개설 약관 동의가 필요합니다.");
		}
		Map<String, Object> ocr = readOcr(requireAuth(memberNo));
		Map<String, Object> flow = flowMap(ocr);
		flow.put("accountConsentAgreed", true);
		ocr.put(FLOW_KEY, flow);
		writeOcr(memberNo, ocr);
		return status(memberNo);
	}

	public Map<String, Object> savePassword(long memberNo, String password) {
		if (password == null || !password.matches("\\d{6}")) {
			throw new IllegalArgumentException("계좌 비밀번호는 숫자 6자리여야 합니다.");
		}
		Map<String, Object> ocr = readOcr(requireAuth(memberNo));
		Map<String, Object> flow = flowMap(ocr);
		flow.put("accountPassword", aesCryptoUtil.encryptGcmToString(password));
		ocr.put(FLOW_KEY, flow);
		writeOcr(memberNo, ocr);
		return status(memberNo);
	}

	public Map<String, Object> savePurpose(long memberNo, String purpose) {
		if (!PURPOSE_CODES.contains(purpose)) {
			throw new IllegalArgumentException("계좌 개설 목적이 올바르지 않습니다.");
		}
		Map<String, Object> ocr = readOcr(requireAuth(memberNo));
		Map<String, Object> flow = flowMap(ocr);
		flow.put("accountPurpose", purpose);
		ocr.put(FLOW_KEY, flow);
		writeOcr(memberNo, ocr);
		return status(memberNo);
	}

	public AccountDto openAccount(long memberNo) {
		Map<String, Object> status = status(memberNo);
		if (!bool(status.get("readyToOpen"))) {
			throw new IllegalStateException("계좌 개설 선행 인증 단계가 완료되지 않았습니다.");
		}

		Map<String, Object> flow = readFlow(requireAuth(memberNo));
		String purpose = (String) flow.get("accountPurpose");
		String encryptedPassword = (String) flow.get("accountPassword");
		return accountService.openDemandDepositAccount(memberNo, purpose, encryptedPassword);
	}

	public int cancel(long memberNo) {
		return accountOpeningDao.deleteIncompleteByMemberNo(memberNo);
	}

	@SuppressWarnings("unchecked")
	private boolean callFastApi(String path, long memberNo, String timeoutCode) {
		String url = fastApiBaseUrl + path + "?pk={pk}";
		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class, memberNo);
			Map<String, Object> body = response.getBody();
			return body != null && Boolean.TRUE.equals(body.get("result"));
		}catch(ResourceAccessException e) {
			if (isTimeout(e)) {
				log.warn("FastAPI account opening verification timed out: path={}, memberNo={}",
						path,
						memberNo
				);
				throw new FastApiVerificationException(timeoutCode, FASTAPI_TIMEOUT_MESSAGE);
			}
			log.warn("FastAPI account opening verification access failed: path={}, memberNo={}, exception={}",
					path,
					memberNo,
					e.getClass().getSimpleName()
			);
			return false;
		}catch(RestClientException e) {
			log.warn("FastAPI account opening verification failed: path={}, memberNo={}, exception={}",
					path,
					memberNo,
					e.getClass().getSimpleName()
			);
			return false;
		}
	}

	private boolean isTimeout(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof SocketTimeoutException) {
				return true;
			}
			String message = current.getMessage();
			if (message != null && message.toLowerCase().contains("timed out")) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	public static class FastApiVerificationException extends IllegalStateException {
		private final String code;

		FastApiVerificationException(String code, String message) {
			super(message);
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	private GcmPayload encryptFile(MultipartFile file) {
		try {
			return aesCryptoUtil.encryptGcm(file.getBytes());
		}catch(Exception e) {
			throw new IllegalStateException("이미지 암호화에 실패했습니다.", e);
		}
	}

	private void requireImage(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			throw new IllegalArgumentException("이미지 파일이 필요합니다.");
		}

		if (image.getSize() <= 0) {
			throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
		}
	}

	private AccountOpeningAuthDto requireAuth(long memberNo) {
		AccountOpeningAuthDto auth = accountOpeningDao.findByMemberNo(memberNo);
		if (auth == null) {
			throw new IllegalStateException("계좌 개설 진행 정보가 없습니다.");
		}
		return auth;
	}

	private SecurityCardDto requireSecurityCard(long memberNo) {
		SecurityCardDto card = securityCardDao.findActiveByMemberNo(memberNo);
		if (card == null || !hasText(card.getSec_num())) {
			throw new IllegalStateException("사용 가능한 보안카드가 없습니다.");
		}
		return card;
	}

	private List<String> parseSecurityCard(String encryptedSecurityCard) {
		String plain = encryptedSecurityCard.split("\\.", -1).length == 3
				? aesCryptoUtil.decryptGcmToString(encryptedSecurityCard)
				: aesCryptoUtil.decrypt(encryptedSecurityCard);

		return parseSecurityCardNumbers(plain);
	}

	private String decryptSecurityCardRaw(String encryptedSecurityCard) {
		return encryptedSecurityCard.split("\\.", -1).length == 3
				? aesCryptoUtil.decryptGcmToString(encryptedSecurityCard)
				: aesCryptoUtil.decrypt(encryptedSecurityCard);
	}

	private List<String> parseSecurityCardNumbers(String plain) {
		List<String> numbers = List.of(plain.trim().split("\\s+"));

		if (numbers.size() != SECURITY_CARD_NUMBER_COUNT) {
			throw new IllegalStateException("Security card format is invalid. Expected 20 numbers.");
		}

		return numbers;
	}

	private List<List<String>> toSecurityCardPairs(List<String> numbers) {
		if (numbers.size() != SECURITY_CARD_NUMBER_COUNT) {
			throw new IllegalStateException("Security card format is invalid. Expected 20 numbers.");
		}

		List<List<String>> pairs = new ArrayList<>();

		for (int i = 0; i < numbers.size(); i += SECURITY_CARD_PAIR_SIZE) {
			pairs.add(List.of(numbers.get(i), numbers.get(i + 1)));
		}

		return pairs;
	}

	private void validateSecurityCardPairIndex(Integer index, String label) {
		if (index == null || index < 0 || index >= SECURITY_CARD_PAIR_COUNT) {
			throw new IllegalStateException("Security card challenge is missing or expired: " + label);
		}
	}

	private String securityCardSide(Object value, String defaultSide) {
		String side = value == null ? "" : String.valueOf(value).trim();
		if (SECURITY_CARD_SIDE_FRONT.equals(side) || SECURITY_CARD_SIDE_BACK.equals(side)) {
			return side;
		}
		return defaultSide;
	}

	private String securityCardPairPart(List<String> pair, String side) {
		if (SECURITY_CARD_SIDE_BACK.equals(side)) {
			return pair.get(1);
		}
		return pair.get(0);
	}

	private Integer nullableIntValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.intValue();
		}
		try {
			return Integer.valueOf(String.valueOf(value));
		}catch(Exception e) {
			return null;
		}
	}

	// TODO: 테스트 후 제거 - decrypted security card values are logged temporarily for mismatch diagnosis.
	private void logSecurityCardPayload(long memberNo, String encryptedSecurityCard, String decryptedSecurityCard, List<String> numbers) {
		String[] gcmParts = encryptedSecurityCard == null ? new String[0] : encryptedSecurityCard.split("\\.", -1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] memberNo={}", memberNo);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] encrypted sec_num exists={}", hasText(encryptedSecurityCard));
		log.warn("[SECURITY_CARD_DEBUG_TEMP] nonce exists={}", gcmParts.length == 3 && hasText(gcmParts[1]));
		log.warn("[SECURITY_CARD_DEBUG_TEMP] tag exists={}", gcmParts.length == 3 && hasText(gcmParts[2]));
		log.warn("[SECURITY_CARD_DEBUG_TEMP] decrypted security card raw={}", decryptedSecurityCard);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] parsed security card list={}", numbers);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] parsed item count={}", numbers.size());
	}

	// TODO: 테스트 후 제거 - security card challenge comparison values are logged temporarily.
	private void logSecurityCardChallenge(
			int index1,
			String side1,
			String expectedFullValue1,
			String expectedPart1,
			int index2,
			String side2,
			String expectedFullValue2,
			String expectedPart2
	) {
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge index1={}", index1 + 1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge side1={}", side1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge expectedFullValue1={}", expectedFullValue1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge expectedPart1={}", expectedPart1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge index2={}", index2 + 1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge side2={}", side2);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge expectedFullValue2={}", expectedFullValue2);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] challenge expectedPart2={}", expectedPart2);
	}

	// TODO: 테스트 후 제거 - security card verify comparison values are logged temporarily.
	private void logSecurityCardVerify(
			int index1,
			String side1,
			String input1,
			String expectedFullValue1,
			String expectedPart1,
			boolean match1,
			int index2,
			String side2,
			String input2,
			String expectedFullValue2,
			String expectedPart2,
			boolean match2
	) {
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify received index1={}", index1 + 1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify received side1={}", side1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify input1={}", input1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify expectedFullValue1={}", expectedFullValue1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify expectedPart1={}", expectedPart1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify match1={}", match1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify received index2={}", index2 + 1);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify received side2={}", side2);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify input2={}", input2);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify expectedFullValue2={}", expectedFullValue2);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify expectedPart2={}", expectedPart2);
		log.warn("[SECURITY_CARD_DEBUG_TEMP] verify match2={}", match2);
	}

	private String cleanAnswer(String value) {
		return value == null ? "" : value.trim();
	}

	private String nextStep(Map<String, Object> status) {
		if (!bool(status.get("privacyConsent"))) return "PRIVACY_CONSENT";
		if (!bool(status.get("idCardUploaded"))) return "ID_CARD";
		if (!bool(status.get("ocrReady"))) return "OCR_WAIT";
		if (!bool(status.get("ocrConfirmed"))) return "OCR_CONFIRM";
		if (!bool(status.get("faceVerified"))) return "FACE";
		if (!bool(status.get("securityCardVerified"))) return "SECURITY_CARD";
		if (!bool(status.get("accountConsent"))) return "ACCOUNT_CONSENT";
		if (!bool(status.get("passwordSet"))) return "PASSWORD";
		if (!bool(status.get("purposeSet"))) return "PURPOSE";
		return "READY_TO_OPEN";
	}

	private Map<String, Object> readFlow(AccountOpeningAuthDto auth) {
		Map<String, Object> ocr = readOcr(auth);
		return flowMap(ocr);
	}

	private Map<String, Object> readOcr(AccountOpeningAuthDto auth) {
		if (auth == null || !hasText(auth.getOcrResultEnc())) {
			return new LinkedHashMap<>();
		}

		try {
			String json = new String(
					aesCryptoUtil.decryptGcm(auth.getOcrResultEnc(), auth.getOcrResultNonce(), auth.getOcrResultTag()),
					StandardCharsets.UTF_8
			);
			return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
		}catch(Exception e) {
			return new LinkedHashMap<>();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> flowMap(Map<String, Object> ocr) {
		Object flow = ocr.get(FLOW_KEY);
		if (flow instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}

		Map<String, Object> newFlow = new LinkedHashMap<>();
		ocr.put(FLOW_KEY, newFlow);
		return newFlow;
	}

	private void writeOcr(long memberNo, Map<String, Object> ocr) {
		try {
			byte[] jsonBytes = objectMapper.writeValueAsBytes(ocr);
			GcmPayload payload = aesCryptoUtil.encryptGcm(jsonBytes);
			validateEncryptedPayload("ocr", payload);
			int updated = accountOpeningDao.updateOcrResult(memberNo, payload.enc(), payload.nonce(), payload.tag());
			if (updated == 0) {
				insertBaseRowIfMissing(memberNo);
				updated = accountOpeningDao.updateOcrResult(memberNo, payload.enc(), payload.nonce(), payload.tag());
			}
			if (updated == 0 || accountOpeningDao.existsOcrResult(memberNo) != 1) {
				if (friendlyMessagesEnabled()) {
					throw new IllegalStateException(OCR_SAVE_FAILED);
				}
				throw new IllegalStateException("OCR 결과 저장에 실패했습니다.");
			}
		}catch(Exception e) {
			throw new IllegalStateException("OCR 결과 저장에 실패했습니다.", e);
		}
	}

	private void savePrivacyConsentValue(long memberNo, String agreed) {
		int updated = accountOpeningDao.updatePrivacyConsent(memberNo, agreed);
		if (updated == 0) {
			insertBaseRowIfMissing(memberNo);
			updated = accountOpeningDao.updatePrivacyConsent(memberNo, agreed);
		}
		if (updated == 0) {
			if (friendlyMessagesEnabled()) {
				throw new IllegalStateException(PRIVACY_CONSENT_SAVE_FAILED);
			}
			throw new IllegalStateException("Privacy consent save failed.");
		}
	}

	private boolean friendlyMessagesEnabled() {
		return true;
	}

	private void insertBaseRowIfMissing(long memberNo) {
		try {
			accountOpeningDao.insertAuthenticationBase(memberNo);
		}catch(Exception ignored) {
			// Another request may have inserted the base row first; retry the update.
		}
	}

	private void runRequiresNew(Runnable action) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		template.executeWithoutResult(status -> action.run());
	}

	private void validateEncryptedPayload(String label, GcmPayload payload) {
		if (payload == null
				|| !hasText(payload.enc())
				|| !hasText(payload.nonce())
				|| !hasText(payload.tag())) {
			throw new IllegalStateException(label + " 이미지 암호화에 실패했습니다.");
		}
	}

	private int length(String value) {
		return value == null ? 0 : value.length();
	}

	private boolean bool(Object value) {
		return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
	}

	private int intValue(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		try {
			return Integer.parseInt(String.valueOf(value));
		}catch(Exception e) {
			return 0;
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}