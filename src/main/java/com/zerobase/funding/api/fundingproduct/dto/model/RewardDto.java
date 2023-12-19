package com.zerobase.funding.api.fundingproduct.dto.model;

import com.zerobase.funding.domain.reward.entity.Reward;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardDto {
    private String title;
    private String description;
    private Integer price;
    private Integer stockQuantity;

    public static RewardDto fromEntity(Reward reward) {
        return RewardDto.builder()
                .title(reward.getTitle())
                .description(reward.getDescription())
                .price(reward.getPrice())
                .stockQuantity(reward.getStockQuantity())
                .build();
    }
}
