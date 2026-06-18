package com.example.bnk.controller.api.chatbot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.common.FinanceDictionaryChatResponseDto;
import com.example.bnk.dto.common.FinanceDictionaryDto;
import com.example.bnk.service.common.FinanceDictionaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class DictionaryAiController {

    private final FinanceDictionaryService dictionaryService;

    @PostMapping("/dictionary")
    public ResponseEntity<Map<String, Object>> dictionary(@RequestBody Map<String, String> payload) {

        String question = payload.get("query");

        Map<String, Object> result = new HashMap<>();

        if (question == null || question.trim().isEmpty()) {
            result.put("answer", "질문을 입력해 주세요.");
            result.put("status", "NOT_FOUND");
            return ResponseEntity.ok(result);
        }

        if (dictionaryService.isCategoryListQuestion(question)) {
            List<FinanceDictionaryDto> categoryResults =
                    dictionaryService.searchCategoryDictionariesForChat(question);

            if (categoryResults != null && !categoryResults.isEmpty()) {
                String answer = makeListAnswer(categoryResults);

                result.put("answer", answer);
                result.put("status", "MULTI_FOUND");
                result.put("candidates", categoryResults);

                return ResponseEntity.ok(result);
            }
        }

        List<FinanceDictionaryDto> results =
                dictionaryService.searchDictionariesForChat(question);

        if (results == null || results.isEmpty()) {
            result.put("answer", "등록된 금융용어사전에서는 해당 내용을 찾을 수 없습니다.");
            result.put("status", "NOT_FOUND");
            return ResponseEntity.ok(result);
        }

        if (results.size() >= 2 && isCompareQuestion(question)) {
            for (FinanceDictionaryDto dto : results) {
                dictionaryService.increaseViewCount(dto.getDictionary_no());
            }

            String answer = makeCompareFallbackAnswer(results);

            try {
                String llmAnswer =
                        dictionaryService.generateLlmAnswerForCompare(question, results);

                if (llmAnswer != null && !llmAnswer.trim().isEmpty()) {
                    answer = llmAnswer;
                }
            } catch (Exception e) {
                System.out.println("LLM 비교 답변 생성 실패. 기본 답변 사용.");
            }

            result.put("answer", answer);
            result.put("status", "COMPARE");
            result.put("candidates", results);

            return ResponseEntity.ok(result);
        }

        if (results.size() >= 2) {
            String answer = makeListAnswer(results);

            result.put("answer", answer);
            result.put("status", "MULTI_FOUND");
            result.put("candidates", results);

            return ResponseEntity.ok(result);
        }

        FinanceDictionaryDto dto = results.get(0);

        dictionaryService.increaseViewCount(dto.getDictionary_no());

        FinanceDictionaryChatResponseDto chatResponse =
                FinanceDictionaryChatResponseDto.found(dto);

        String answer = chatResponse.getAnswer();

        try {
            String llmAnswer =
                    dictionaryService.generateLlmAnswerForSingle(question, dto);

            if (llmAnswer != null && !llmAnswer.trim().isEmpty()) {
                answer = llmAnswer;
            }
        } catch (Exception e) {
            System.out.println("LLM 단일 답변 생성 실패. 기본 답변 사용.");
        }

        result.put("answer", answer);
        result.put("status", "FOUND");
        result.put("dictionaryNo", dto.getDictionary_no());
        result.put("dictionaryName", dto.getDictionary_nm());
        result.put("category", dto.getDictionary_category());

        return ResponseEntity.ok(result);
    }

    private String makeListAnswer(List<FinanceDictionaryDto> results) {
        StringBuilder answer = new StringBuilder();

        answer.append("질문과 관련된 금융용어 목록입니다.\n\n");

        for (int i = 0; i < results.size(); i++) {
            FinanceDictionaryDto dto = results.get(i);

            answer.append(i + 1)
                    .append(". ")
                    .append(dto.getDictionary_nm())
                    .append(" - ")
                    .append(dto.getDictionary_category())
                    .append("\n");
        }

        answer.append("\n더 자세히 알고 싶은 용어를 입력해 주세요.");

        return answer.toString();
    }

    private String makeCompareFallbackAnswer(List<FinanceDictionaryDto> results) {
        StringBuilder answer = new StringBuilder();

        answer.append("질문하신 용어들의 차이는 다음과 같습니다.\n\n");

        for (FinanceDictionaryDto dto : results) {
            answer.append("[")
                    .append(dto.getDictionary_nm())
                    .append("]\n")
                    .append(dto.getDictionary_content())
                    .append("\n\n");
        }

        return answer.toString();
    }

    private boolean isCompareQuestion(String question) {
        if (question == null) {
            return false;
        }

        return question.contains("차이")
                || question.contains("비교")
                || question.contains("다른")
                || question.contains("달라");
    }
}