package com.example.bnk.controller.api.employee.manager;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dao.product.IProductTermsDao;
import com.example.bnk.dto.employee.EmployeeDto;
import com.example.bnk.dto.product.ProductTermsDto;
import com.example.bnk.dto.product.suggestion.ApprovedSuggestionDetailDto;
import com.example.bnk.service.employees.EmployeeListService;
import com.example.bnk.service.employees.EmployeeLogService;
import com.example.bnk.service.product.ApprovedSuggestionService;

@RestController
@RequestMapping("/api/employee/approved")
public class ApprovedApiController {
	
	@Autowired
	ApprovedSuggestionService approvedService;
	@Autowired
	private EmployeeListService empService;
	@Autowired
	private EmployeeLogService logService;
	@Autowired
	private IProductTermsDao termDto;
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	
	// 승인한 제안서 목록 가져오기
	@GetMapping("/approvedList")
	public List<ApprovedSuggestionDetailDto> approvedList(
			@AuthenticationPrincipal String username
			) {
		
		// 1. 내정보 가져오기 
		EmployeeDto myInfo = empService.findByUsername(username);
		
		//로그
		String logKey = "사원 번호:"+myInfo.getEmployee_no();
		logService.build(username, "SELECT", "TB_APPROVED_SUGGESTION", logKey, "승인된 제안서 목록을 불러온다.");
		
		// 2. 내가 승인한 상품
		List<ApprovedSuggestionDetailDto> list = approvedService.approvedList(myInfo.getEmployee_no());
		
		return list;
	}
	
	// 승인한 제안서 상세
	@GetMapping("/approvedDetail")
	public ApprovedSuggestionDetailDto approvedDetail(
			@RequestParam("suggestion_no") long suggestion_no,
			@AuthenticationPrincipal String username
			) {
		
		ApprovedSuggestionDetailDto detail = approvedService.approvedDetail(suggestion_no);
		//로그
		String logKey = "제안서 번호:"+suggestion_no;
		logService.build(username, "SELECT", "TB_APPROVED_SUGGESTION", logKey, "승인된 제안서 목록을 불러온다.");
		
		return detail;
	}
	
	/*
	 * // 약관 상세
	 * 
	 * @GetMapping("/termsDetail") public ProductTermsDto termsDetail(
	 * 
	 * @RequestParam("suggestion_no") long suggestion_no ) {
	 * 
	 * ProductTermsDto dto = termDto.detail(suggestion_no);
	 * 
	 * return dto; }
	 */
	@GetMapping("/termsDetail")
	public ResponseEntity<Map<String, Object>> termsDetail(
	        @RequestParam("suggestion_no") long suggestion_no) {

	    ProductTermsDto dto = termDto.detail(suggestion_no);
	    System.out.println("=== [termsDetail] dto 조회 결과: " + dto);

	    Map<String, Object> result = new HashMap<>();

	    result.put("terms_no", dto.getTerms_no());
	    result.put("terms_title", dto.getTerms_title());
	    result.put("terms_type", dto.getTerms_type());
	    result.put("terms_summary", dto.getTerms_summary());
	    result.put("terms_version", dto.getTerms_version());
	    result.put("use_yn", dto.getUse_yn());
	    result.put("uploaded_by", dto.getUploaded_by());
	    result.put("uploaded_at", dto.getUploaded_at());
	    result.put("updated_at", dto.getUpdated_at());
	    result.put("pdf_url", dto.getPdf_url());
	    System.out.println("=== [termsDetail] pdf_url: " + dto.getPdf_url());

	    try {
	        String pdfPath = dto.getPdf_url();
	        Path path = Paths.get(uploadPath + pdfPath.replace("/upload/", "")); // 경로 설정
	        System.out.println("=== [termsDetail] 실제 파일 경로: " + path.toAbsolutePath());
	        System.out.println("=== [termsDetail] 파일 존재 여부: " + path.toFile().exists());

	        byte[] pdfBytes = Files.readAllBytes(path);
	        System.out.println("=== [termsDetail] PDF 바이트 크기: " + pdfBytes.length);

	        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
	        result.put("pdf_base64", base64Pdf);
	        System.out.println("=== [termsDetail] Base64 변환 완료, 길이: " + base64Pdf.length());

	    } catch (IOException e) {
	        System.out.println("=== [termsDetail] PDF 변환 실패: " + e.getMessage());
	        result.put("pdf_base64", null);
	    }

	    return ResponseEntity.ok(result);
	}

	@GetMapping("/pdf")
	public ResponseEntity<List<String>> getPdfPages(
	        @RequestParam("suggestion_no") long suggestion_no) {

	    ProductTermsDto dto = termDto.detail(suggestion_no);
	    System.out.println("=== [getPdfPages] dto 조회 결과: " + dto);

	    String pdfPath = dto.getPdf_url();
	    System.out.println("=== [getPdfPages] pdf_url: " + pdfPath);

	    List<String> base64Pages = new ArrayList<>();

	    try {
	    	Path path = Paths.get(uploadPath + pdfPath.replace("/upload/", "")); // 경로 설정
	        System.out.println("=== [getPdfPages] 실제 파일 경로: " + path.toAbsolutePath());
	        System.out.println("=== [getPdfPages] 파일 존재 여부: " + path.toFile().exists());

	        PDDocument document = PDDocument.load(path.toFile());
	        System.out.println("=== [getPdfPages] PDF 총 페이지 수: " + document.getNumberOfPages());

	        PDFRenderer renderer = new PDFRenderer(document);

	        for (int i = 0; i < document.getNumberOfPages(); i++) {
	            BufferedImage image = renderer.renderImageWithDPI(i, 150);
	            System.out.println("=== [getPdfPages] " + (i+1) + "페이지 렌더링 완료");

	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ImageIO.write(image, "JPG", baos);
	            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
	            base64Pages.add(base64);
	            System.out.println("=== [getPdfPages] " + (i+1) + "페이지 Base64 변환 완료, 길이: " + base64.length());
	        }

	        document.close();
	        System.out.println("=== [getPdfPages] 전체 완료, 총 " + base64Pages.size() + "페이지");

	    } catch (IOException e) {
	        System.out.println("=== [getPdfPages] 오류 발생: " + e.getMessage());
	        e.printStackTrace();
	        return ResponseEntity.status(500).build();
	    }

	    return ResponseEntity.ok(base64Pages);
	}
	
	
}
