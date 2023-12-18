package com.zerobase.funding.common.builder;

import static com.zerobase.funding.common.constants.MemberConstants.EMAIL;
import static com.zerobase.funding.common.constants.MemberConstants.NAME;
import static com.zerobase.funding.common.constants.MemberConstants.PROFILE;

import com.zerobase.funding.api.member.dto.model.AddressDto;
import com.zerobase.funding.api.member.dto.model.MemberDto;

public class MemberDtoBuilder {
    public static MemberDto memberDto() {
        return MemberDto.builder()
                .name(NAME)
                .email(EMAIL)
                .profile(PROFILE)
                .address(AddressDto.builder().build())
                .build();
    }
}
