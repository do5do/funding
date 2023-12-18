package com.zerobase.funding.api.member.dto.model;

import com.zerobase.funding.domain.member.entity.Address;
import lombok.Builder;

@Builder
public record AddressDto(
        String roadAddress,
        String addressDetail,
        String zipcode
) {

    public static AddressDto fromEntity(Address address) {
        return AddressDto.builder()
                .roadAddress(address.getRoadAddress())
                .addressDetail(address.getAddressDetail())
                .zipcode(address.getZipcode())
                .build();
    }

    public Address toEntity() {
        return Address.builder()
                .roadAddress(roadAddress)
                .addressDetail(addressDetail)
                .zipcode(zipcode)
                .build();
    }
}
