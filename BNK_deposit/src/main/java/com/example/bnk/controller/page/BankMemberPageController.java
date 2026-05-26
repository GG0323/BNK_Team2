package com.example.bnk.controller.page;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bnk.auth.MemberDetails;
import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.AccountTransactionDto;
import com.example.bnk.dto.member.BankMemberDto;
import com.example.bnk.dto.member.MemberProductDto;
import com.example.bnk.dto.member.MemberTrackingLogDto;
import com.example.bnk.service.member.AccountService;
import com.example.bnk.service.member.AccountTransactionService;
import com.example.bnk.service.member.BankMemberService;
import com.example.bnk.service.member.MemberTrackingLogService;
import com.example.bnk.service.product.ProductSalesService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class BankMemberPageController {
	
	private final BankMemberService bankMemberService;
	private final AccountService accountService;
	private final ProductSalesService productSalesService;
	private final MemberTrackingLogService memberTrackingLogService;
	private final AccountTransactionService accountTransactionService;
	
	// 고객 마이페이지 
	@GetMapping("/mypage")
    public String rootMembersMypage(Model model, SecurityContextHolder secu) {
	
        // 회원 정보 조회
        String currentLoginId = secu.getContext().getAuthentication().getName(); 
        BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
        long currentMemberNo = memberInfo.getMember_no();
        
        // 계좌 정보 조회
        List<AccountDto> accountList = accountService.getAccounts(memberInfo.getLogin_id());
        int accountCount = accountList.size();
        long totalBalance = accountList.stream().mapToLong(AccountDto::getBalance).sum();
        int logCount = memberTrackingLogService.getLogCount(currentMemberNo);
        
        // 가입 상품 개수 조회
        int productCount = productSalesService.getSubscribedProductCount(currentMemberNo);
        
        // 최근 접속 기록 조회
        List<MemberTrackingLogDto> recentLogs = memberTrackingLogService.getRecentLogs(currentMemberNo);
        
        model.addAttribute("member", memberInfo);
        model.addAttribute("accountCount", accountCount);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("productCount", productCount);
        model.addAttribute("recentLogs", recentLogs);
        model.addAttribute("logCount", logCount);
        model.addAttribute("pageName", "mypage");
        
        return "member/mypage";
    }
	
	// 고객의 내 정보 페이지
	@GetMapping("/myinfo")
	public String rootMembersMyinfo(Model model, SecurityContextHolder secu) {
		String currentLoginId = secu.getContext().getAuthentication().getName();
		BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
		model.addAttribute("member", memberInfo);
		model.addAttribute("pageName", "myinfo");
        
        return "member/myinfo";
	}
	
	// 고객의 내 정보 수정하기 페이지
	@GetMapping("/myinfo/edit")
	public String rootMembersMyinfoEdit(Model model, SecurityContextHolder secu) {
		String currentLoginId = secu.getContext().getAuthentication().getName();
	    BankMemberDto memberInfo = bankMemberService.getMemberInfo(currentLoginId);
	    
	    model.addAttribute("member", memberInfo);
	    model.addAttribute("pageName", "myinfo"); // 서브 네비게이션 '2. 내정보' 활성화 유지
	    
	    return "member/myinfo_edit";
	}
	
	// 고객의 내 정보 수정, DB 업데이트 기능
	@PostMapping("/myinfo/update")
	public String updateMyInfo(
	        @RequestParam(value = "phone_number", defaultValue = "") String phoneNumber,
	        @RequestParam(value = "email", defaultValue = "") String email,
	        @RequestParam(value = "address_main", defaultValue = "") String addressMain,
	        @RequestParam(value = "address_detail", defaultValue = "") String addressDetail,
	        RedirectAttributes rttr, SecurityContextHolder secu) {
	    
		String currentLoginId = secu.getContext().getAuthentication().getName(); 
	    
	    // 입력 데이터가 아예 없으면 DB 접근 차단 (Early Return)
	    if (phoneNumber.trim().isEmpty() && email.trim().isEmpty() && addressMain.trim().isEmpty()) {
	        rttr.addFlashAttribute("error", "수정할 정보가 입력되지 않았습니다.");
	        return "redirect:/myinfo/edit";
	    }

	    // 전화번호 백엔드 검증
	    if (!phoneNumber.matches("^010-\\d{4}-\\d{4}$")) {
	        rttr.addFlashAttribute("error", "전화번호 형식이 올바르지 않거나 조작되었습니다.");
	        return "redirect:/myinfo/edit";
	    }

	    // 이메일 백엔드 검증 (이메일이 비어있지 않은 경우에만 검증하도록 유연성 추가)
	    if (!email.trim().isEmpty() && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
	        rttr.addFlashAttribute("error", "이메일 형식이 올바르지 않거나 조작되었습니다.");
	        return "redirect:/myinfo/edit";
	    }
	    
	    String fullAddress = addressMain;
	    if (addressDetail != null && !addressDetail.trim().isEmpty()) {
	        fullAddress += " " + addressDetail;
	    }

	    // 검증을 모두 통과한 데이터만 DTO에 세팅하여 전송
	    BankMemberDto updateDto = new BankMemberDto();
	    updateDto.setLogin_id(currentLoginId); 
	    updateDto.setPhone_number(phoneNumber);
	    updateDto.setEmail(email);
	    updateDto.setAdress(fullAddress);

	    bankMemberService.modifyMemberInfo(updateDto);

	    rttr.addFlashAttribute("msg", "개인정보가 성공적으로 수정되었습니다.");
	    return "redirect:/myinfo";
	}
		
	// 고객 비밀번호 수정, DB 업데이트 기능
	@PostMapping("/myinfo/update-password")
	public String updatePassword(
	        @RequestParam(value = "current_password", defaultValue = "") String currentPassword,
	        @RequestParam(value = "new_password", defaultValue = "") String newPassword,
	        RedirectAttributes rttr, SecurityContextHolder secu) 
	{
		String currentLoginId = secu.getContext().getAuthentication().getName();
	    
	    // 비밀번호 입력값이 비어있으면 DB 접근 차단
	    if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
	        rttr.addFlashAttribute("error", "비밀번호를 정확히 입력해주세요.");
	        return "redirect:/myinfo/edit";
	    }
	    
	    // 서비스 계층에 비밀번호 변경 요청
	    boolean isChanged = bankMemberService.changePassword(currentLoginId, currentPassword, newPassword);
	    
	    if (isChanged) {
	        rttr.addFlashAttribute("msg", "비밀번호가 성공적으로 변경되었습니다.");
	        return "redirect:/myinfo"; 
	    } else {
	        rttr.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
	        return "redirect:/myinfo/edit";
	    }
	}
	
	// 고객의 내 계좌 정보 보기
	@GetMapping("/myaccounts")
	public String rootMembersAccounts(Model model, SecurityContextHolder secu) {
		
		// 현재 로그인된 회원 번호
	    String username = secu.getContext().getAuthentication().getName();
	    
	    // 계좌 목록 조회
	    List<AccountDto> accountList = accountService.getAccounts(username);
	    
	    // 모델에 데이터 및 활성화 탭 이름 전달
	    model.addAttribute("accountList", accountList);
	    model.addAttribute("pageName", "myaccounts"); // 서브 네비게이션 3번 활성화
	    
	    return "member/myaccounts";
	}
	
	@GetMapping("/myhistory")
    public String rootMembersHistory(
    		@RequestParam(value = "accountNo", required = false) Long accountNo, // 필수 여부를 false로 변경
            RedirectAttributes rttr, 
            Model model) {
		
		// 만약 상단 네비게이션 탭을 통해 파라미터 없이 들어왔다면 계좌 목록으로 튕겨냄
	    if (accountNo == null) {
	        rttr.addFlashAttribute("msg", "조회할 계좌를 먼저 선택해 주세요.");
	        return "redirect:/myaccounts";
	    }
        
        // 서비스에 심부름을 시켜 데이터를 가져옵니다.
	    AccountDto account = accountService.getAccountDetail(accountNo.longValue());
	    List<AccountTransactionDto> transactionList = accountTransactionService.getTransactions(accountNo.longValue());
        
        // 가져온 데이터를 HTML(Thymeleaf)이 읽을 수 있게 Model에 예쁘게 담아줍니다.
        // (이름을 "account", "transactionList"로 담았기 때문에 HTML에서 ${account...}로 꺼내 쓸 수 있습니다!)
        model.addAttribute("account", account);
        model.addAttribute("transactionList", transactionList);
        model.addAttribute("pageName", "myhistory"); 
        
        return "member/myhistory";
    }
	
	// 고객의 가입 상품 내역 페이지 조회
	@GetMapping("/myproducts")
	public String rootMembersProducts(Model model, SecurityContextHolder secu) {
	    //  현재 로그인한 회원 번호 세팅 (테스트용 1번 회원)
	    String username = secu.getContext().getAuthentication().getName();
	    // 서비스 호출하여 JOIN된 가입 상품 리스트 가져오기
	    List<MemberProductDto> productList = productSalesService.getSubscribedProducts(username);
	    
	    // Thymeleaf HTML 화면으로 데이터 보내기
	    model.addAttribute("productList", productList);
	    model.addAttribute("pageName", "myproducts"); // 5번 탭 활성화 암호
	    
	    return "member/myproducts";
	}
}
