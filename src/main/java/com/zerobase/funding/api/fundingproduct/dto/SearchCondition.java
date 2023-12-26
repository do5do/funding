package com.zerobase.funding.api.fundingproduct.dto;

import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.api.fundingproduct.type.SortType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record SearchCondition(
        @NotNull
        FilterType filterType,

        @Nullable
        SortType sortType
) {
}
