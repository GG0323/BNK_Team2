package com.example.bnk.dto.product.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ollama API 응답 DTO
 * /api/generate 응답 body
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOllamaResponseDto {

    // Ollama 모델명
    private String model;

    // 생성된 응답 텍스트
    private String response;

    // 응답 완료 여부
    private boolean done;

    // Ollama가 내부적으로 내려주는 부가 정보들
    // 지금 기능에서는 필수는 아니지만, 응답 매핑 오류 방지용으로 둠
    private String created_at;
    private String done_reason;

    private Long total_duration;
    private Long load_duration;
    private Integer prompt_eval_count;
    private Long prompt_eval_duration;
    private Integer eval_count;
    private Long eval_duration;
}