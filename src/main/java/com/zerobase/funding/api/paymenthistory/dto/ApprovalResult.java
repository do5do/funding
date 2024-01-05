package com.zerobase.funding.api.paymenthistory.dto;

import jakarta.validation.constraints.NotNull;

public record ApprovalResult(
        @NotNull
        Long fundingId,

        @NotNull
        Integer amount
) {

}
