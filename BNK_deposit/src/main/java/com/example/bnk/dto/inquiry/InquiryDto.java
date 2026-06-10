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
public class InquiryDto {
	
	private long  inquiry_no;
	private long bank_member_no;
	private String inquiry_category;
	private String inquiry_title;
	private String inquiry_status;
	private LocalDate created_at;
	private LocalDate updated_at;
	
	
}
