package com.example.bnk.controller.api.member;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.MemberProductDto;
import com.example.bnk.service.product.ProductTermination;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class ProductTerminateApiController {
	
	@Autowired
	private final ProductTermination productTermination;
	
	@PostMapping("/terminate")
	public ResponseEntity<Map<String, String>> productTermination(
					@RequestParam("member_no") long member_no, // 회원정보
					@RequestParam("account_no") long account_no){ // 해지할 계좌 고유번호
		
		// 세팅
		// 결과 알려줄거.
		Map<String, String> result = new LinkedHashMap<>();

		// 해지할 계좌의 정보
		AccountDto terminateAc = productTermination.findAccountByAccountNo(account_no);
		System.out.println("member_no: " + member_no);
		System.out.println("account_no: " + account_no);
		if(terminateAc == null) {
			result.put("result", "failed");
			result.put("message", "해지할 계좌 정보가 없습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
		}
		
		
		// 이자를 포함한 환금가격 담을거
		long balance = terminateAc.getBalance();
		
		MemberProductDto prd = productTermination.selectUserProduct(member_no, account_no);	// 가입한 상품의 정보들
		double applied_interest_rate = prd.getApplied_interest_rate();	// 얘는 적용 금리
		
		
		// 유저 주계좌 고유번호
		long linked_account_no;
		
		
		linked_account_no = productTermination.findUsingAccountNo(member_no); 
		if(linked_account_no == 0) {
			System.out.println("dto가 NULL입니다.");
			result.put("result", "failed");
			result.put("message", "주계좌를 조회할 수 없습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
		}
		
		
		int resulted = productTermination.terminate(linked_account_no, balance, applied_interest_rate, account_no, member_no);
		if(resulted == 0) {
			System.out.println("모종의 이유로 실패");
			result.put("result", "failed");
			
			return ResponseEntity.badRequest().body(result);
		}

		result.put("result", "success");
		result.put("message", "완료되었습니다.");
		
		return ResponseEntity.ok(result);
	}
}
