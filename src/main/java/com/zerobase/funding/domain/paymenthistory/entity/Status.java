package com.zerobase.funding.domain.paymenthistory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    COMPLETE("결제완료"),
    CANCEL("결제취소");

    private final String description;
}
