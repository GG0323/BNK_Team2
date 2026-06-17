package com.example.bnk.dto.product.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ollama API 요청 DTO
 * /api/generate 호출 시 사용할 요청 body
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOllamaRequestDto {

    // 사용할 Ollama 모델명
    // 예: llama3.1, llama3, gemma2, qwen2.5 등
    private String model;

    // Ollama에게 전달할 프롬프트
    private String prompt;

    // false면 응답을 한 번에 받음
    // true면 stream 방식으로 조각조각 받음
    private boolean stream;
}