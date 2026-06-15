package com.example.bnk.dto.inquiry;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InquiryMsgDto {

	private long msg_no;
	private long inquiry_no;
	private String sender_type;
	private String sender_id;
	private String msg_content;
	private LocalDate msg_created_at;
	
	
}
