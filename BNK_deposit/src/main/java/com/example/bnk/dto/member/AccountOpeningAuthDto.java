package com.example.bnk.dto.member;

import lombok.Data;

@Data
public class AccountOpeningAuthDto {
	private long memberNo;
	private String idcardImageEnc;
	private String idcardImageNonce;
	private String idcardImageTag;
	private String idfaceImageEnc;
	private String idfaceImageNonce;
	private String idfaceImageTag;
	private String ocrResultEnc;
	private String ocrResultNonce;
	private String ocrResultTag;
	private String faceImageEnc;
	private String faceImageNonce;
	private String faceImageTag;
	private Double similarityScore;
	private String privacyConsentYn;
}
