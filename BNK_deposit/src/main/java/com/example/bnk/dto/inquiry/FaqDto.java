package com.example.bnk.dto.inquiry;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor		
public class FaqDto {
	
	private long   faq_no;
	private String faq_category;
	private String faq_question;
	private String faq_answer;
	private int    faq_order;
	
	private LocalDate created_at;
	private LocalDate updated_at;
	private String    created_by;
	private String    updated_by;
	
}
