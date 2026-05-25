package com.example.bnk.controller.page;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bnk.dto.common.FinanceDictionaryDto;
import com.example.bnk.service.common.FinanceDictionaryService;

@Controller
@CrossOrigin(origins = "*")
public class FinanceDictionaryController {
	
	private final FinanceDictionaryService dictionaryService;
	
	public FinanceDictionaryController(FinanceDictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	// 금융용어 사전 페이지 이동 및 검색
	@GetMapping("/financedictionary")
    public String rootFinDictionary(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<FinanceDictionaryDto> list;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있으면 검색 결과 가져오기
            list = dictionaryService.searchDictionary(keyword);
            model.addAttribute("keyword", keyword); // 검색창에 검색어 유지용
        } else {
            // 검색어가 없으면 전체 목록 가져오기
            list = dictionaryService.getAllDictionarys();
        }
        
        model.addAttribute("dictionaryList", list);
        return "common/financedictionary"; 
    }
	
	// 금융용어 사전 상세 페이지
	@GetMapping("/financedictionary/{dictionary_no}")
	public String detailFinDictionary(@PathVariable("dictionary_no") int dictionary_no, Model model) {
	    // 서비스에게 번호를 주고 해당 용어 데이터를 가져오라고 시킵니다.
		FinanceDictionaryDto financeword = dictionaryService.getDictionary(dictionary_no);
	    
	    // 가져온 데이터를 'term'이라는 이름으로 화면에 넘깁니다.
	    model.addAttribute("financeword", financeword);
	    
	    // 상세 화면용 HTML 파일(findictionaryDetail.html)을 엽니다.
	    return "common/financedictionarydetail"; 
	}
	
	// ✨ 1. 등록 폼 화면 띄우기 (C)
	@GetMapping("/financedictionary/write")
	public String writeForm() {
		return "common/financedictionary_write";
	}
	
	// ✨ 2. 작성한 데이터 DB에 저장하기 (C)
	@PostMapping("/financedictionary/write")
	public String writeProcess(FinanceDictionaryDto dto) {
		dictionaryService.addDictionary(dto);
		return "redirect:/financedictionary"; // 작성 완료 후 목록으로 튕겨냄
	}
	
	// ✨ 3. 수정 폼 화면 띄우기 (U)
	@GetMapping("/financedictionary/edit/{dictionary_no}")
	public String editForm(@PathVariable("dictionary_no") long dictionary_no, Model model) {
		// 조회수 증가 없는 메서드 사용!
		FinanceDictionaryDto financeword = dictionaryService.getDictionaryForEdit(dictionary_no);
		model.addAttribute("financeword", financeword);
		return "common/financedictionary_edit";
	}
	
	// ✨ 4. 수정한 데이터 DB에 덮어쓰기 (U)
	@PostMapping("/financedictionary/edit")
	public String editProcess(FinanceDictionaryDto dto) {
		dictionaryService.modifyDictionary(dto);
		// 수정 완료 후 수정한 그 단어의 상세 페이지로 다시 이동
		return "redirect:/financedictionary/" + dto.getDictionary_no(); 
	}
	
	// ✨ 5. 데이터 삭제하기 (D)
	@GetMapping("/financedictionary/delete/{dictionary_no}")
	public String deleteProcess(@PathVariable("dictionary_no") long dictionary_no) {
		dictionaryService.removeDictionary(dictionary_no);
		return "redirect:/financedictionary"; // 삭제 후 목록으로 이동
	}
}