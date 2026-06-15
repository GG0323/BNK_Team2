package com.example.bnk.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PersonaProductLogStatDto {
	
    private String persona;        // 청년 / 중년 남성 / 중년 여성 / 노년 ... (매퍼의 personaCase 가 만든 라벨)
    private long product_no;
    private String product_name;
    private String product_type;
    private long view_cnt;         // 상품 상세 조회수
    private long member_cnt;       // 조회한 회원 수 (중복 제거)

}
