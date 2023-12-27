package com.zerobase.funding.api.fundingproduct.dto.model;

import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record FundingProductDto(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer targetAmount,
        Integer views,
        List<RewardDto> rewards,
        List<ImageDto> images
) {


    public static FundingProductDto fromEntity(FundingProduct fundingProduct, Integer views) {
        return FundingProductDto.builder()
                .id(fundingProduct.getId())
                .title(fundingProduct.getTitle())
                .description(fundingProduct.getDescription())
                .startDate(fundingProduct.getStartDate())
                .endDate(fundingProduct.getEndDate())
                .targetAmount(fundingProduct.getTargetAmount())
                .views(views)
                .rewards(fundingProduct.getRewards().stream()
                        .map(RewardDto::fromEntity).toList())
                .images(fundingProduct.getImages().stream()
                        .map(ImageDto::fromEntity).toList())
                .build();
    }
}
