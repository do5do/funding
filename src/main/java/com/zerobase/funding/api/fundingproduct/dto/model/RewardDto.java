package com.zerobase.funding.api.fundingproduct.dto.model;

import com.zerobase.funding.domain.reward.entity.Reward;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RewardDto(
        @NotBlank
        String title,

        @NotBlank
        String description,

        @NotNull
        Integer price,

        @NotNull
        Integer stockQuantity
) {

    public static RewardDto fromEntity(Reward reward) {
        return RewardDto.builder()
                .title(reward.getTitle())
                .description(reward.getDescription())
                .price(reward.getPrice())
                .stockQuantity(reward.getStockQuantity())
                .build();
    }

    public Reward toEntity() {
        return Reward.builder()
                .title(title)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .build();
    }
}
