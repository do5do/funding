package com.zerobase.funding.api.paymenthistory.controller;

import com.zerobase.funding.api.paymenthistory.dto.ApprovalResult;
import com.zerobase.funding.api.paymenthistory.service.PaymentHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment-history")
@RequiredArgsConstructor
@RestController
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    /**
     * 임의 결제 승인 요청 결과 저장 api <br>
     * 결제 관련 api를 제공하지 않기 때문에
     * 결제 흐름에서 마지막 단계인 승인 요청 결과에 대해 저장하는 부분만 임의로 작성하였습니다.
     * @param request 결제 승인 요청 결과
     * @return void
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<Void> paymentApprovalResult(
            @RequestBody @Valid ApprovalResult request) {
        paymentHistoryService.paymentApprovalResult(request);
        return ResponseEntity.noContent().build();
    }
}
