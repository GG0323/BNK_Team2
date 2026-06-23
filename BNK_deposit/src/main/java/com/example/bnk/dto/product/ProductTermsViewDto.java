package com.example.bnk.dto.product;

import lombok.Data;

@Data
public class ProductTermsViewDto {

    private long terms_no;
    private Long product_no;

    private String terms_title;
    private String terms_type;

    private String pdf_url;

    private String terms_summary;
    private String terms_version;

    private String use_yn;
}