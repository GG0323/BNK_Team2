package com.example.bnk.controller.page;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.member.MemberTrackingLogDto;
import com.example.bnk.service.member.AccountService;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.member.MemberTrackingLogService;
import com.example.bnk.service.product.ProductSalesService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BankMemberPageController {
	
	private final BankMemberService bankMemberService;
	private final AccountService accountService;
	private final ProductSalesService productSalesService;
	private final MemberTrackingLogService memberTrackingLogService;

	@GetMapping("/mypage")
    public String rootMembersMypage(Model model) {
        // 1. 회원 정보 조회
        String currentLoginId = "dev_hyun"; 
        BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
        long currentMemberNo = memberInfo.getMember_no();
        
        // 2. 계좌 정보 조회
        List<AccountDto> accountList = accountService.getAccounts(currentMemberNo);
        int accountCount = accountList.size();
        long totalBalance = accountList.stream().mapToLong(AccountDto::getBalance).sum();
        int logCount = memberTrackingLogService.getLogCount(currentMemberNo);
        
        // 3. 가입 상품 개수 조회
        int productCount = productSalesService.getSubscribedProductCount(currentMemberNo);
        
        // 4. 최근 접속 기록 조회 (새로 추가된 부분)
        List<MemberTrackingLogDto> recentLogs = memberTrackingLogService.getRecentLogs(currentMemberNo);
        
        // 5. 화면으로 모든 데이터 바인딩
        model.addAttribute("member", memberInfo);
        model.addAttribute("accountCount", accountCount);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("productCount", productCount);
        model.addAttribute("recentLogs", recentLogs);
        model.addAttribute("logCount", logCount);
        model.addAttribute("pageName", "mypage");
        
        return "members/mypage";
    }
	
	@GetMapping("/myinfo")
	public String rootMembersMyinfo(Model model) {
		String currentLoginId = "dev_hyun";
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		memberInfo.setMember_identifier("980515-1******"); // 임시로 만들어놓음
		model.addAttribute("member", memberInfo);
		model.addAttribute("pageName", "myinfo");
        
        return "members/myinfo";
	}
	
	@GetMapping("/myinfo/edit")
	public String rootMembersMyinfoEdit(Model model) {
	    String currentLoginId = "dev_hyun";
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
	    
	    // 식별번호 임시 마스킹 처리 (이전과 동일)
	    memberInfo.setMember_identifier("980515-1******");
	    
	    model.addAttribute("member", memberInfo);
	    model.addAttribute("pageName", "myinfo"); // 서브 네비게이션 '2. 내정보' 활성화 유지
	    
	    return "members/myinfo_edit";
	}
	
	@PostMapping("/myinfo/update")
	public String updateMyInfo(
	        @RequestParam("phone_number") String phoneNumber,
	        @RequestParam("email") String email,
	        @RequestParam("address_main") String addressMain,
	        @RequestParam("address_detail") String addressDetail,
	        RedirectAttributes rttr) {
	    // 1. [핵심 보안] HTML에서 사용자가 변조해서 보냈을지도 모를 login_id 파라미터는 아예 받지 않습니다!
	    // 대신, 서버가 보증하는 "현재 로그인한 아이디(세션)"를 직접 꺼내옵니다.
	    String currentLoginId = "dev_hyun"; 
	    
	    // 전화번호 백엔드 검증 (010-숫자4개-숫자4개)
	    if (!phoneNumber.matches("^010-\\d{4}-\\d{4}$")) {
	        rttr.addFlashAttribute("error", "전화번호 형식이 올바르지 않거나 조작되었습니다.");
	        return "redirect:/myinfo/edit";
	    }

	    // 이메일 백엔드 검증 (기본적인 이메일 형태)
	    if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
	        rttr.addFlashAttribute("error", "이메일 형식이 올바르지 않거나 조작되었습니다.");
	        return "redirect:/myinfo/edit";
	    }
	    
	    String fullAddress = addressMain;
	    if (addressDetail != null && !addressDetail.trim().isEmpty()) {
	        fullAddress += " " + addressDetail;
	    }

	    BankMemberDto updateDto = new BankMemberDto();
	    updateDto.setLogin_id(currentLoginId); // 폼 데이터가 아닌, 서버가 알고 있는 세션 아이디 셋팅
	    updateDto.setPhone_number(phoneNumber);
	    updateDto.setEmail(email);
	    updateDto.setAdress(fullAddress);

	    bankMemberService.modifyMemberInfo(updateDto);

	    rttr.addFlashAttribute("msg", "개인정보가 성공적으로 수정되었습니다.");
	    return "redirect:/myinfo";
	}
	
	@PostMapping("/myinfo/update-password")
	public String updatePassword(
	        @RequestParam("current_password") String currentPassword,
	        @RequestParam("new_password") String newPassword,
	        RedirectAttributes rttr // 리다이렉트 시 화면에 알림 메시지를 전달하기 위한 객체
	) {
	    String currentLoginId = "dev_hyun"; 
	    
	    // 서비스 계층에 비밀번호 변경 요청
	    boolean isChanged = bankMemberService.changePassword(currentLoginId, currentPassword, newPassword);
	    
	    if (isChanged) {
	        // 성공 시: 조회 화면으로 이동하며 성공 메시지 전달 (추후 myinfo.html 에도 알림 스크립트 추가 필요)
	        rttr.addFlashAttribute("msg", "비밀번호가 성공적으로 변경되었습니다.");
	        return "redirect:/myinfo"; 
	    } else {
	        // 실패 시: 기존 수정 화면으로 되돌아가며 에러 메시지 전달
	        rttr.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
	        return "redirect:/myinfo/edit";
	    }
	}
}
