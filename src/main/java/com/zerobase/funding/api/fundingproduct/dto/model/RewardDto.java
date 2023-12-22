package com.zerobase.funding.api.fundingproduct.dto.model;

import com.zerobase.funding.domain.reward.entity.Reward;
import lombok.Builder;

@Builder
public record RewardDto(
        String title,
        String description,
        Integer price,
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
