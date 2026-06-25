package com.example.bnk.dao.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.bnk.dto.member.AccountOpeningAuthDto;

@Mapper
public interface IAccountOpeningDao {
	AccountOpeningAuthDto findByMemberNo(@Param("memberNo") long memberNo);

	int insertAuthenticationBase(@Param("memberNo") long memberNo);

	int updatePrivacyConsent(@Param("memberNo") long memberNo, @Param("agreed") String agreed);

	int updateIdCardImage(
			@Param("memberNo") long memberNo,
			@Param("idcardImageEnc") String idcardImageEnc,
			@Param("idcardImageNonce") String idcardImageNonce,
			@Param("idcardImageTag") String idcardImageTag
	);

	int existsIdCardImage(@Param("memberNo") long memberNo);

	int updateFaceImage(
			@Param("memberNo") long memberNo,
			@Param("faceImageEnc") String faceImageEnc,
			@Param("faceImageNonce") String faceImageNonce,
			@Param("faceImageTag") String faceImageTag
	);

	int existsFaceImage(@Param("memberNo") long memberNo);

	int updateOcrResult(
			@Param("memberNo") long memberNo,
			@Param("ocrResultEnc") String ocrResultEnc,
			@Param("ocrResultNonce") String ocrResultNonce,
			@Param("ocrResultTag") String ocrResultTag
	);

	int existsOcrResult(@Param("memberNo") long memberNo);

	int deleteIncompleteByMemberNo(@Param("memberNo") long memberNo);
}
