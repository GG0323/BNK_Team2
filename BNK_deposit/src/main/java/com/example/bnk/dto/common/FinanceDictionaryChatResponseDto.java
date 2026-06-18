package com.example.bnk.dto.common;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDictionaryChatResponseDto {

    private String agent;
    private String status;
    private Long dictionaryNo;
    private String dictionaryName;
    private String category;
    private String answer;
    private List<Candidate> candidates;

    public static FinanceDictionaryChatResponseDto found(FinanceDictionaryDto dto) {
        FinanceDictionaryChatResponseDto response = new FinanceDictionaryChatResponseDto();

        response.agent = "finance-dictionary-agent";
        response.status = "FOUND";
        response.dictionaryNo = dto.getDictionary_no();
        response.dictionaryName = dto.getDictionary_nm();
        response.category = dto.getDictionary_category();
        response.answer = "'" + dto.getDictionary_nm() + "'에 대한 설명입니다.\n\n"
                + dto.getDictionary_content();
        response.candidates = null;

        return response;
    }

    public static FinanceDictionaryChatResponseDto notFound() {
        FinanceDictionaryChatResponseDto response = new FinanceDictionaryChatResponseDto();

        response.agent = "finance-dictionary-agent";
        response.status = "NOT_FOUND";
        response.dictionaryNo = null;
        response.dictionaryName = null;
        response.category = null;
        response.answer = "등록된 금융용어사전에서는 해당 내용을 찾을 수 없습니다.";
        response.candidates = new ArrayList<>();

        return response;
    }

    public static FinanceDictionaryChatResponseDto compare(String answer, List<FinanceDictionaryDto> results) {
        FinanceDictionaryChatResponseDto response = new FinanceDictionaryChatResponseDto();

        response.agent = "finance-dictionary-agent";
        response.status = "COMPARE";
        response.dictionaryNo = null;
        response.dictionaryName = null;
        response.category = null;
        response.answer = answer;
        response.candidates = toCandidates(results);

        return response;
    }

    public static FinanceDictionaryChatResponseDto multiFound(String answer, List<FinanceDictionaryDto> results) {
        FinanceDictionaryChatResponseDto response = new FinanceDictionaryChatResponseDto();

        response.agent = "finance-dictionary-agent";
        response.status = "MULTI_FOUND";
        response.dictionaryNo = null;
        response.dictionaryName = null;
        response.category = null;
        response.answer = answer;
        response.candidates = toCandidates(results);

        return response;
    }

    private static List<Candidate> toCandidates(List<FinanceDictionaryDto> results) {
        List<Candidate> candidates = new ArrayList<>();

        if (results == null) {
            return candidates;
        }

        for (FinanceDictionaryDto dto : results) {
            candidates.add(new Candidate(
                    dto.getDictionary_no(),
                    dto.getDictionary_nm(),
                    dto.getDictionary_category()
            ));
        }

        return candidates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private long dictionaryNo;
        private String dictionaryName;
        private String category;
    }
}