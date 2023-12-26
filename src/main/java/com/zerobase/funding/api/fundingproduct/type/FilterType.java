package com.zerobase.funding.api.fundingproduct.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilterType {
    UPCOMING("오픈예정"),
    IN_PROGRESS("진행중");

    private final String description;
}
