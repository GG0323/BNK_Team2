package com.example.bnk.controller.page;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bnk.dto.common.FinanceDictionaryDto;
import com.example.bnk.service.common.FinanceDictionaryService;

@Controller
@RequestMapping("/bnk")
@CrossOrigin(origins = "*")
public class FinanceDictionaryController {
	
	private final FinanceDictionaryService dictionaryService;
	
	public FinanceDictionaryController(FinanceDictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@GetMapping("/findictionary")
    public String rootFinDictionary(Model model) {
        System.out.println("active rootFinDictionary()...");
        List<FinanceDictionaryDto> list = dictionaryService.getAllDictionarys();
        model.addAttribute("dictionaryList", list);
        return "dictionary/findictionary"; 
    }
	
	@GetMapping("/findictionary/{dictionary_no}")
	public String detailFinDictionary(@PathVariable("dictionary_no") int dictionary_no, Model model) {
	    // 서비스에게 번호를 주고 해당 용어 데이터를 가져오라고 시킵니다.
		FinanceDictionaryDto financeword = dictionaryService.getDictionary(dictionary_no);
	    
	    // 가져온 데이터를 'term'이라는 이름으로 화면에 넘깁니다.
	    model.addAttribute("financeword", financeword);
	    
	    // 상세 화면용 HTML 파일(findictionaryDetail.html)을 엽니다.
	    return "dictionary/findictionaryDetail"; 
	}
}