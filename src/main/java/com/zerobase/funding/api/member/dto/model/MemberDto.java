package com.zerobase.funding.api.member.dto.model;

import com.zerobase.funding.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private String name;
    private String email;
    private String profile;
    private AddressDto address;

    public static MemberDto fromEntity(Member member) {
        MemberDto memberDto = MemberDto.builder()
                .name(member.getName())
                .email(member.getEmail())
                .profile(member.getProfile())
                .build();

        if (member.getAddress() != null) {
            memberDto.addAddress(AddressDto.fromEntity(member.getAddress()));
        }
        return memberDto;
    }

    private void addAddress(AddressDto address) {
        this.address = address;
    }
}
