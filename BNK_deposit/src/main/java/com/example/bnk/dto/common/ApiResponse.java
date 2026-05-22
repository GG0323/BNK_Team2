package com.example.bnk.dto.common;

import lombok.Getter;

/**
 * 웹/앱 공통으로 사용하는 API 응답 표준 포맷.
 * 모바일 앱에서 success / message / data 만 일관되게 파싱하면 되도록 통일한다.
 *
 * 예) 성공: { "success": true,  "message": null, "data": { ... } }
 *     실패: { "success": false, "message": "에러 메시지", "data": null }
 */
@Getter
public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;

	private ApiResponse(boolean success, String message, T data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	// 데이터만 담아서 성공 응답
	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, null, data);
	}

	// 메시지 + 데이터를 담아서 성공 응답
	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}

	// 메시지만 담아서 성공 응답 (수정/변경 완료 등)
	public static ApiResponse<Void> success(String message) {
		return new ApiResponse<>(true, message, null);
	}

	// 실패 응답
	public static ApiResponse<Void> fail(String message) {
		return new ApiResponse<>(false, message, null);
	}
}