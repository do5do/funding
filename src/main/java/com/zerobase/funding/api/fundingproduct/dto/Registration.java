package com.zerobase.funding.api.fundingproduct.dto;

import com.zerobase.funding.api.fundingproduct.dto.model.RewardDto;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record Registration() {

    public record Request(
            @NotBlank
            String title,

            @NotBlank
            String description,

            @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,

            @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,

            @NotNull
            Integer targetAmount,

            @Valid @NotNull
            List<RewardDto> rewards
    ) {

        public FundingProduct toEntity() {
            return FundingProduct.builder()
                    .title(title)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .targetAmount(targetAmount)
                    .build();
        }
    }
}
