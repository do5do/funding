package com.zerobase.funding.api.funding.dto;

import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import lombok.Builder;

@Builder
public record PredicatedResponse(
        Integer fundingPrice,
        Status status,
        Long rewardId,
        String rewardTitle
) {

    public static PredicatedResponse fromEntity(Funding funding) {
        return PredicatedResponse.builder()
                .fundingPrice(funding.getFundingPrice())
                .status(funding.getStatus())
                .rewardId(funding.getReward().getId())
                .rewardTitle(funding.getReward().getTitle())
                .build();
    }
}
