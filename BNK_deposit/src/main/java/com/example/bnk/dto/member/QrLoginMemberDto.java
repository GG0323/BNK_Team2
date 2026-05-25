package com.example.bnk.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QrLoginMemberDto {

    // QR 로그인 성공 시 세션에 저장할 회원 정보

    private long member_no;          // 회원 번호
    private String login_id;         // 로그인 아이디
    private String member_name;      // 회원 이름
    private String member_type;      // 회원 유형
    private String member_status;    // 회원 상태
}