package com.example.bnk.controller.api.common;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DuplicateKeyException;

import com.example.bnk.dto.common.ApiResponse;
import com.example.bnk.dto.common.FinanceDictionaryDto;
import com.example.bnk.service.common.FinanceDictionaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FinanceDictionaryApiController {

    private final FinanceDictionaryService dictionaryService;

    // 금융용어 목록 조회 + 검색
    @GetMapping("/api/financedictionary")
    public ResponseEntity<ApiResponse<?>> getDictionaryList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", required = false, defaultValue = "all") String searchType) {

        List<FinanceDictionaryDto> list;

        if (keyword != null && !keyword.trim().isEmpty()) {
            list = dictionaryService.searchDictionaryByType(searchType, keyword);
        } else {
            list = dictionaryService.getAllDictionarys();
        }

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // 금융용어 상세 조회
    @GetMapping("/api/financedictionary/{dictionary_no}")
    public ResponseEntity<ApiResponse<?>> getDictionaryDetail(
            @PathVariable("dictionary_no") int dictionaryNo) {

        FinanceDictionaryDto financeword = dictionaryService.getDictionary(dictionaryNo);

        if (financeword == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("금융용어 정보를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.ok(financeword));
    }

    // 수정 화면에서 사용할 조회수 증가 없는 상세 조회
    @GetMapping("/api/financedictionary/edit/{dictionary_no}")
    public ResponseEntity<ApiResponse<?>> getDictionaryForEdit(
            @PathVariable("dictionary_no") long dictionaryNo) {

        FinanceDictionaryDto financeword = dictionaryService.getDictionaryForEdit(dictionaryNo);

        if (financeword == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("수정할 금융용어 정보를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.ok(financeword));
    }

    // 금융용어 등록
    @PostMapping("/api/financedictionary")
    public ResponseEntity<ApiResponse<Void>> addDictionary(
            @ModelAttribute FinanceDictionaryDto dto) {

        try {
            dictionaryService.addDictionary(dto);

            return ResponseEntity.ok(
                    ApiResponse.success("금융용어가 등록되었습니다.")
            );

        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("이미 등록된 금융용어입니다."));
        }
    }

    // 금융용어 수정
    @PostMapping("/api/financedictionary/edit")
    public ResponseEntity<ApiResponse<Void>> modifyDictionary(
            @ModelAttribute FinanceDictionaryDto dto) {

        dictionaryService.modifyDictionary(dto);

        return ResponseEntity.ok(ApiResponse.success("금융용어가 수정되었습니다."));
    }

    // 금융용어 삭제
    @DeleteMapping("/api/financedictionary/{dictionary_no}")
    public ResponseEntity<ApiResponse<Void>> removeDictionary(
            @PathVariable("dictionary_no") long dictionaryNo) {

        dictionaryService.removeDictionary(dictionaryNo);

        return ResponseEntity.ok(ApiResponse.success("금융용어가 삭제되었습니다."));
    }
}