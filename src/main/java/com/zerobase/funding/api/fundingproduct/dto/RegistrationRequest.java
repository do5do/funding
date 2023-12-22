package com.zerobase.funding.api.fundingproduct.dto;

import com.zerobase.funding.api.fundingproduct.dto.model.RewardDto;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Builder
public record RegistrationRequest(
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
        Integer targetAmount,

        @Valid @NotEmpty
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
