package com.example.bnk.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BankMemberLogDto {
	
	private long member_tracking_log_no;
	private long member_no;
	private String requested_page;
	private String request_method;
	private String request_url;
	private String request_ip;
	private String accessed_at;
	
}
