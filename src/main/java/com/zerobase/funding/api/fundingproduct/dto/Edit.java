package com.zerobase.funding.api.fundingproduct.dto;

import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record Edit() {

    public record Request(
            @NotBlank
            String title,

            @NotBlank
            String description,

            @NotNull
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,

            @NotNull
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,

            @NotNull
            Integer targetAmount
    ) {

    }

    @Builder
    public record Response(
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Integer targetAmount
    ) {

        public static Response fromEntity(FundingProduct fundingProduct) {
            return Response.builder()
                    .title(fundingProduct.getTitle())
                    .description(fundingProduct.getDescription())
                    .startDate(fundingProduct.getStartDate())
                    .endDate(fundingProduct.getEndDate())
                    .targetAmount(fundingProduct.getTargetAmount())
                    .build();
        }
    }
}
