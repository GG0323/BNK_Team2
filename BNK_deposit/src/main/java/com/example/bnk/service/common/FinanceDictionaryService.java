package com.example.bnk.service.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.common.IFinanceDictionaryDao;
import com.example.bnk.dto.common.FinanceDictionaryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinanceDictionaryService {

	private final IFinanceDictionaryDao financeDictionaryDao;
	private final OpenAiDictionaryLlmService dictionaryLlmService;
	
	// 금융용어사전 리스트 출력
	@Transactional(readOnly = true)
	public List<FinanceDictionaryDto> getAllDictionarys(){
		return financeDictionaryDao.selectAllDictionarys();
	}
	
	// 금융용어 상세보기 페이지 (✨ 로직 업그레이드!)
	@Transactional
	public FinanceDictionaryDto getDictionary(long dictionary_no) {
	    // 해당 용어의 조회수를 DB에서 먼저 1 증가. (UPDATE)
	    financeDictionaryDao.updateViewCount(dictionary_no);
	    
	    // 조회수가 올라간 최신 상태의 데이터를 가져와서 컨트롤러로 반환. (SELECT)
	    return financeDictionaryDao.selectDictionaryByNo(dictionary_no);
	}
	
	// 검색 로직 추가
	@Transactional(readOnly = true)
	public List<FinanceDictionaryDto> searchDictionary(String keyword) {
		return financeDictionaryDao.searchDictionary(keyword);
	}
	
	// ✨ 수정 화면용 데이터 불러오기 (조회수 증가 없음!)
	@Transactional(readOnly = true)
    public FinanceDictionaryDto getDictionaryForEdit(long dictionary_no) {
        return financeDictionaryDao.selectDictionaryByNo(dictionary_no);
    }

	// ✨ 용어 등록
	@Transactional
	public void addDictionary(FinanceDictionaryDto dto) {
		financeDictionaryDao.insertDictionary(dto);
	}
	
	// ✨ 용어 수정
	@Transactional
	public void modifyDictionary(FinanceDictionaryDto dto) {
		financeDictionaryDao.updateDictionary(dto);
	}
	
	// ✨ 용어 삭제
	@Transactional
	public void removeDictionary(long dictionary_no) {
		financeDictionaryDao.deleteDictionary(dictionary_no);
	}
	
	// 검색 분류
	@Transactional(readOnly = true)
	public List<FinanceDictionaryDto> searchDictionaryByType(String searchType, String keyword) {

	    if (keyword == null || keyword.trim().isEmpty()) {
	        return getAllDictionarys();
	    }

	    if (searchType == null || searchType.trim().isEmpty()) {
	        searchType = "all";
	    }

	    return financeDictionaryDao.searchDictionaryByType(searchType, keyword.trim());
	}
	
	// 챗봇용 메서드
	public FinanceDictionaryDto searchBestDictionaryForChat(String question) {

	    String keyword = extractKeyword(question);

	    List<FinanceDictionaryDto> results =
	            financeDictionaryDao.searchDictionaryForChat(question, keyword);

	    if (results == null || results.isEmpty()) {
	        return null;
	    }

	    List<FinanceDictionaryDto> validResults = new ArrayList<>();

	    for (FinanceDictionaryDto dto : results) {
	        if (isValidTermMatch(question, keyword, dto.getDictionary_nm())) {
	            validResults.add(dto);
	        }
	    }

	    if (validResults.isEmpty()) {
	        return null;
	    }

	    FinanceDictionaryDto best = validResults.get(0);

	    financeDictionaryDao.updateViewCount(best.getDictionary_no());

	    return best;
	}
	
	// 챗봇용 여러 금융용어 검색
	@Transactional(readOnly = true)
	public List<FinanceDictionaryDto> searchDictionariesForChat(String question) {

	    String keyword = extractKeyword(question);

	    List<FinanceDictionaryDto> results =
	            financeDictionaryDao.searchDictionaryForChat(question, keyword);

	    if (results == null || results.isEmpty()) {
	        return new ArrayList<>();
	    }

	    List<FinanceDictionaryDto> validResults = new ArrayList<>();

	    for (FinanceDictionaryDto dto : results) {
	        if (isValidTermMatch(question, keyword, dto.getDictionary_nm())) {
	            validResults.add(dto);
	        }
	    }

	    return validResults;
	}

	// 조회수 증가용 메서드
	@Transactional
	public void increaseViewCount(long dictionaryNo) {
	    financeDictionaryDao.updateViewCount(dictionaryNo);
	}
	
	private String extractKeyword(String question) {
	    if (question == null) {
	        return "";
	    }

	    return question
	            .replace("이 뭐야", "")
	            .replace("가 뭐야", "")
	            .replace("은 뭐야", "")
	            .replace("는 뭐야", "")
	            .replace("뭐야", "")
	            .replace("알려줘", "")
	            .replace("설명해줘", "")
	            .replace("?", "")
	            .trim();
	}
	
	private boolean isValidTermMatch(String question, String keyword, String term) {
	    if (question == null || term == null || term.trim().isEmpty()) {
	        return false;
	    }

	    String cleanedKeyword = keyword == null ? "" : keyword.trim();
	    String cleanedTerm = term.trim();

	    // 사용자가 정확히 용어명을 입력한 경우
	    if (cleanedKeyword.equals(cleanedTerm)) {
	        return true;
	    }

	    // 질문 문장 안에서 용어가 독립적으로 등장하는 경우
	    return containsAsTerm(question, cleanedTerm);
	}

	private boolean containsAsTerm(String question, String term) {
	    int index = question.indexOf(term);

	    while (index >= 0) {
	        int start = index;
	        int end = index + term.length();

	        boolean leftOk = start == 0 || !isKoreanOrAlphaNumeric(question.charAt(start - 1));
	        boolean rightOk = end >= question.length()
	                || !isKoreanOrAlphaNumeric(question.charAt(end))
	                || isAllowedJosa(question.charAt(end));

	        if (leftOk && rightOk) {
	            return true;
	        }

	        index = question.indexOf(term, index + 1);
	    }

	    return false;
	}

	private boolean isKoreanOrAlphaNumeric(char ch) {
	    return (ch >= '가' && ch <= '힣')
	            || (ch >= 'A' && ch <= 'Z')
	            || (ch >= 'a' && ch <= 'z')
	            || (ch >= '0' && ch <= '9');
	}

	private boolean isAllowedJosa(char ch) {
	    return ch == '이'
	            || ch == '가'
	            || ch == '은'
	            || ch == '는'
	            || ch == '을'
	            || ch == '를'
	            || ch == '와'
	            || ch == '과'
	            || ch == '도'
	            || ch == '만'
	            || ch == '에'
	            || ch == '의'
	            || ch == '로'
	            || ch == '랑';
	}
	
	@Transactional(readOnly = true)
	public List<FinanceDictionaryDto> searchCategoryDictionariesForChat(String question) {
	    if (question == null || question.trim().isEmpty()) {
	        return new ArrayList<>();
	    }

	    String category = extractCategoryFromQuestion(question);

	    if (category == null) {
	        return new ArrayList<>();
	    }

	    return financeDictionaryDao.selectDictionaryByCategory(category);
	}

	private String extractCategoryFromQuestion(String question) {
	    if (question.contains("금리")) {
	        return "금리";
	    }

	    if (question.contains("해지") || question.contains("만기")) {
	        return "만기/해지";
	    }

	    if (question.contains("가입")) {
	        return "가입조건";
	    }

	    if (question.contains("납입")) {
	        return "납입";
	    }

	    if (question.contains("세금") || question.contains("보호") || question.contains("비과세")) {
	        return "세금/보호";
	    }

	    if (question.contains("상품") || question.contains("통장")) {
	        return "상품유형";
	    }

	    if (question.contains("예금") || question.contains("적금") || question.contains("예적금")) {
	        return "예적금";
	    }

	    return null;
	}

	public boolean isCategoryListQuestion(String question) {
	    if (question == null) {
	        return false;
	    }

	    return question.contains("종류")
	            || question.contains("목록")
	            || question.contains("관련")
	            || question.contains("뭐 있어")
	            || question.contains("어떤")
	            || question.contains("리스트");
	}
	
	public String generateLlmAnswerForSingle(String question, FinanceDictionaryDto dto) {
	    String prompt = """
	            [사용자 질문]
	            %s

	            [DB 검색 결과]
	            용어명: %s
	            카테고리: %s
	            설명: %s

	            [답변 지시]
	            위 DB 검색 결과만 사용해서 답변해라.
	            DB에 없는 추가 금융정보는 말하지 마라.
	            답변은 한국어로 자연스럽고 문법에 맞게 작성해라.
	            사용자가 이해하기 쉽게 2문장으로 설명해라.
	            첫 문장은 용어의 뜻을 설명해라.
	            두 번째 문장은 조건, 주의점, 특징 중 DB 설명에 있는 내용을 바탕으로 설명해라.
	            말투는 친절한 설명체로 작성해라.
	            """.formatted(
	            question,
	            dto.getDictionary_nm(),
	            dto.getDictionary_category(),
	            dto.getDictionary_content()
	    );

	    return dictionaryLlmService.generateDictionaryAnswer(prompt);
	}

	public String generateLlmAnswerForCompare(String question, List<FinanceDictionaryDto> results) {
	    StringBuilder dbText = new StringBuilder();

	    for (FinanceDictionaryDto dto : results) {
	        dbText.append("용어명: ").append(dto.getDictionary_nm()).append("\n");
	        dbText.append("카테고리: ").append(dto.getDictionary_category()).append("\n");
	        dbText.append("설명: ").append(dto.getDictionary_content()).append("\n\n");
	    }

	    String prompt = """
	            [사용자 질문]
	            %s

	            [DB 검색 결과]
	            %s

	            [답변 지시]
	            위 DB 검색 결과만 사용해서 각 용어의 차이를 설명해라.
	            먼저 한 문장으로 핵심 차이를 말하고,
	            그 다음 용어별 설명을 짧게 정리해라.
	            DB에 없는 내용은 추측하지 마라.
	            """.formatted(question, dbText.toString());

	    return dictionaryLlmService.generateDictionaryAnswer(prompt);
	}

	public String generateLlmAnswerForMultiFound(String question, List<FinanceDictionaryDto> results) {
	    StringBuilder dbText = new StringBuilder();

	    for (FinanceDictionaryDto dto : results) {
	        dbText.append("- ")
	                .append(dto.getDictionary_nm())
	                .append(" / ")
	                .append(dto.getDictionary_category())
	                .append("\n");
	    }

	    String prompt = """
	            [사용자 질문]
	            %s

	            [DB 검색 결과]
	            %s

	            [답변 지시]
	            사용자가 고를 수 있도록 관련 금융용어 목록을 보기 좋게 정리해라.
	            각 용어의 상세 설명은 길게 쓰지 말고, 목록 중심으로 답해라.
	            DB에 없는 용어는 추가하지 마라.
	            """.formatted(question, dbText.toString());

	    return dictionaryLlmService.generateDictionaryAnswer(prompt);
	}
}
