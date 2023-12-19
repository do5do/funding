package com.zerobase.funding.api.fundingproduct.dto.model;

import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundingProductDto {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer targetAmount;
    private Integer views;
    private List<RewardDto> rewards;
    private List<ImageDto> images;

    public static FundingProductDto fromEntity(FundingProduct fundingProduct) {
        return FundingProductDto.builder()
                .title(fundingProduct.getTitle())
                .description(fundingProduct.getDescription())
                .startDate(fundingProduct.getStartDate())
                .endDate(fundingProduct.getEndDate())
                .targetAmount(fundingProduct.getTargetAmount())
                .views(fundingProduct.getViews())
                .rewards(fundingProduct.getRewards().stream()
                        .map(RewardDto::fromEntity).toList())
                .images(fundingProduct.getImages().stream()
                        .map(ImageDto::fromEntity).toList())
                .build();
    }
}
