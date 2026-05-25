package com.example.bnk.dto.member;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 마이페이지(/api/mypage) 한 화면에 필요한 데이터를 한 번에 묶어 내려주는 응답 DTO.
 * 호출 횟수를 줄여 모바일 앱에서도 단일 요청으로 화면을 구성할 수 있게 한다.
 */
@Getter
@Builder
public class MypageSummaryDto {

	private final BankMemberDto member;        // 회원 기본 정보
	private final int accountCount;            // 보유 계좌 수
	private final long totalBalance;           // 총 잔액
	private final int productCount;            // 가입 상품 수
	private final int logCount;                // 최근 접속 기록 건수
	private final List<MemberTrackingLogDto> recentLogs; // 최근 접속 기록 목록
}