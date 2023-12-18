package com.zerobase.funding.api.member.dto;

import com.zerobase.funding.api.member.dto.model.AddressDto;
import jakarta.validation.constraints.NotBlank;

public record MemberEditRequest(
        @NotBlank String name,
        AddressDto address
) {
}
