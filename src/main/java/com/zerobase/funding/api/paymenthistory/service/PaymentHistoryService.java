package com.zerobase.funding.api.paymenthistory.service;

import static com.zerobase.funding.api.exception.ErrorCode.INVALID_PAYMENT;

import com.zerobase.funding.api.funding.service.FundingService;
import com.zerobase.funding.api.paymenthistory.dto.ApprovalResult;
import com.zerobase.funding.api.paymenthistory.exception.PaymentHistoryException;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.paymenthistory.entity.PaymentHistory;
import com.zerobase.funding.domain.paymenthistory.repository.PaymentHistoryRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final FundingService fundingService;

    @Transactional
    public void paymentApprovalResult(ApprovalResult request) {
        Funding funding = fundingService.getFundingById(request.fundingId());
        validatePayment(request, funding);

        paymentHistoryRepository.save(PaymentHistory.of(request.amount(), funding));
    }

    private static void validatePayment(ApprovalResult request, Funding funding) {
        if (!Objects.equals(funding.getFundingPrice(), request.amount())) {
            throw new PaymentHistoryException(INVALID_PAYMENT);
        }
    }
}
