package com.zerobase.funding.api.funding.dto;

import com.zerobase.funding.api.member.dto.model.AddressDto;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public record CreateFunding() {

    public record Request(
            @NotNull
            Long fundingProductId,

            @NotNull
            Long rewardId,

            @Nullable
            AddressDto address
    ) {

        public Funding toEntity(Integer fundingPrice) {
            return Funding.builder()
                    .status(Status.IN_PROGRESS)
                    .fundingPrice(fundingPrice)
                    .build();
        }
    }

    @Builder
    public record Response(
            String rewardTitle,
            Integer price,
            Long fundingId
    ) {

        public static Response from(String rewardTitle, Integer price, Long fundingId) {
            return Response.builder()
                    .rewardTitle(rewardTitle)
                    .price(price)
                    .fundingId(fundingId)
                    .build();
        }
    }
}
