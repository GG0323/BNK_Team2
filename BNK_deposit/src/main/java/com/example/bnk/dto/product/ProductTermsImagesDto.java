package com.example.bnk.dto.product;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductTermsImagesDto {
	private long productNo;
	private List<TermsFile> terms;

	@Data
	@Builder
	public static class TermsFile {
		private String termsTitle;
		private String fileName;
		private int pageCount;
		private List<TermsPage> pages;
	}

	@Data
	@Builder
	public static class TermsPage {
		private int pageNo;
		private String imageBase64;
	}
}
