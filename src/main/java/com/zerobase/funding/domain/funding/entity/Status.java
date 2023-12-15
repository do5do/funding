package com.zerobase.funding.domain.funding.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Status {
    UPCOMING("오픈예정"),
    IN_PROGRESS("진행중"),
    COMPLETE("완료"),
    FAIL("실패");

    private final String description;
}
