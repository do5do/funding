package com.zerobase.funding.domain.delivery.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    SHIPPING("배송중"),
    COMPLETE("배송완료"),
    CANCEL("배송취소");

    private final String description;
}
