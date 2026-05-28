package com.example.bnk.service.member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bnk.dao.member.IMyProductDao;
import com.example.bnk.dto.member.MyProductDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyProductService {

    private final IMyProductDao myProductDao;

    // 가입상품 목록 조회
    @Transactional
    public List<MyProductDto> getMyProducts(long memberNo) {
    List<MyProductDto> list = myProductDao.findMyProductsByMemberNo(memberNo);

    for (MyProductDto dto : list) {
        fillExpectedAmount(dto);
    }

    return list;
}

    // 가입상품 상세 조회
    @Transactional
    public MyProductDto getMyProductDetail(long memberNo, int subscriptionNo) {
        MyProductDto dto = myProductDao.findMyProductDetail(memberNo, subscriptionNo);

        if (dto == null) {
            return null;
        }

        fillExpectedAmount(dto);

        return dto;
    }

    // 세전 단순 예상 이자와 예상 수령액 계산
    @Transactional
    private void fillExpectedAmount(MyProductDto dto) {
        BigDecimal principal = safe(dto.getSubscription_amount());
        BigDecimal rate = safe(dto.getApplied_interest_rate());

        int months = dto.getSubscription_months();

        if (months <= 0) {
            months = 12;
        }

        BigDecimal interest;

        if ("SAVINGS".equals(dto.getProduct_type())) {
            interest = calculateSavingsInterest(dto, rate, months);
            principal = calculateSavingsPrincipal(dto, principal, months);
        } else {
            interest = calculateDepositInterest(principal, rate, months);
        }

        dto.setExpected_interest(interest);
        dto.setExpected_total_amount(principal.add(interest));
    }

    // 예금 단순 이자 계산
    @Transactional
    private BigDecimal calculateDepositInterest(BigDecimal principal, BigDecimal rate, int months) {
        return principal
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
    }

    // 적금 단순 예상 원금 계산
    @Transactional
    private BigDecimal calculateSavingsPrincipal(MyProductDto dto, BigDecimal fallbackPrincipal, int months) {
        BigDecimal autoTransferAmount = safe(dto.getAuto_transfer_amount());

        if (autoTransferAmount.compareTo(BigDecimal.ZERO) > 0) {
            return autoTransferAmount.multiply(BigDecimal.valueOf(months));
        }

        return fallbackPrincipal;
    }

    // 적금 단순 예상 이자 계산
    @Transactional
    private BigDecimal calculateSavingsInterest(MyProductDto dto, BigDecimal rate, int months) {
        BigDecimal autoTransferAmount = safe(dto.getAuto_transfer_amount());

        if (autoTransferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return calculateDepositInterest(safe(dto.getSubscription_amount()), rate, months);
        }

        BigDecimal monthSum = BigDecimal.valueOf((long) months * (months + 1) / 2);

        return autoTransferAmount
                .multiply(monthSum)
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
    }
    
    @Transactional
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}