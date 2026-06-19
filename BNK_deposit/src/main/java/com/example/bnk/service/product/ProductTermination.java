package com.example.bnk.service.product;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IAccountDao;
import com.example.bnk.dao.product.IProductSalesDao;
import com.example.bnk.dto.member.AccountDto;
import com.example.bnk.dto.member.MemberProductDto;

import lombok.RequiredArgsConstructor;

// 계좌 해지용 서비스
@Service
@Transactional
@RequiredArgsConstructor
public class ProductTermination {

	private final IAccountDao iAccountDao;
	private final IProductSalesDao iProductSalesDao;
	
	
	// 환금 - 상품 비활성 합침
    @Transactional
    public int terminate(
    		long linked_account_no,
    		long balance,
    		double applied_interest_rate,
    		long account_no,
    		long member_no
    		) {
    	
    	long interest = (long)(balance * applied_interest_rate);
    	System.out.println("interest: " + interest);
    	
    	AccountDto linked_account_no_dto = findUsersAccount(linked_account_no);
    	System.out.println("linked_account_no_dto: " + linked_account_no_dto);
    	
    	int interestResult = interestToMyAccount(balance, applied_interest_rate, member_no);
    	System.out.println("환금 결과: " + interestResult);
    	if(interestResult == 0) {
    		System.out.println("환금 실패!");
    		return 0;
    	}
    	
    	
    	int changeStatus = changeAccountStatus(account_no);
    	System.out.println("비활성 변경 결과: " + changeStatus);
    	if(changeStatus == 0) {
    		System.out.println("상품 계좌 비활성 실패!");
    		return 0;
    	}
    	
    	int changeSubStatus = changeSubscription(account_no);
    	System.out.println("해지하기 결과: " + changeSubStatus);
    	if(changeSubStatus == 0) {
    		System.out.println("해지하기가 실패하였습니다!");
    		return 0;
    	}
    	
    	System.out.println("모두 성공");
    	return 1;
    }
    
	
    
	
	// 환급받을 계좌(죽, 주계좌) 찾기
	public long findUsingAccountNo(@Param("member_no") long member_no) {
		System.out.println("주계좌 고유번호 찾기");
		return iAccountDao.findUsingAccountNo(member_no);
	}
	
	// 해지할 상품
    public AccountDto findAccountByAccountNo(@Param("accountNo") long accountNo) {
    	AccountDto result = iAccountDao.findAccountByAccountNo(accountNo);
    	System.out.println("해지할 계좌: " + result);
    	return result;
    }
    
    // account_no로 회원의 주계좌 정보 받기
    public AccountDto findUsersAccount(@Param("account_no") long account_no) {
    	System.out.println("계좌를 불러오겠습니다.");
    	return iAccountDao.findUsersAccount(account_no);
    }
    
    // 회원의 상품 데이터
    public MemberProductDto selectUserProduct(@Param("member_no")long member_no, @Param("account_no") long account_no) {
    	System.out.println("회원이 가입한 특정 상품정보 불러오기");
    	return iProductSalesDao.selectUsersProduct(member_no, account_no);
    }
    
    // 회원의 상품 환금 해주기
    public int interestToMyAccount(
    		@Param("balance") long balance, 
    		@Param("applied_interest_rate") double applied_interest_rate,
    		@Param("member_no") long member_no) {
    	
    	if (iAccountDao.interestToMyAccount(balance, applied_interest_rate, member_no) == 1){
    		// 돈 환금 성공!
    		System.out.println("환금 성공!");
    		return 1;
    	}
    	System.out.println("환금 실패!");
    	return 0;
    }

    // 마지막으로 가입상태 해지하기로 변경
    public int changeSubscription(@Param("account_no") long account_no) {
    	System.out.println("해지하기로 변경");
    	if(iAccountDao.changeSubscriptionStatus(account_no) == 1) {
    		System.out.println("해지하기까지 성공!");
    		return 1;
    	}
    	System.out.println("해지하기 실패 ㅜㅜ");
    	return 0;
    }
    
    // 돈 환금이 끝나면 계좌를 비활성 상태로 변경하기
    public int changeAccountStatus(@Param("account_no") long account_no) {
    	
    	if(iAccountDao.changeAccountStatus(account_no) == 1) {
    		System.out.println("비활성화 상태 만들기 완료!");
    		return 1;
    	}
    	System.out.println("모종의 이유로 비활성화가 되지 않았습니다.");
    	return 0;
    }
    

    
}
