package com.example.bnk.dto.member;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityCardDto {
	private long sec_no;
	private long member_no;
	private String sec_num;
	private LocalDateTime create_at;
	private LocalDateTime expire_at;
	private String use_yn;
}