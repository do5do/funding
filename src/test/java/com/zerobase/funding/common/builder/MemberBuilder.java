package com.zerobase.funding.common.builder;

import static com.zerobase.funding.common.constants.MemberConstants.EMAIL;
import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static com.zerobase.funding.common.constants.MemberConstants.NAME;
import static com.zerobase.funding.common.constants.MemberConstants.PROFILE;

import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.entity.Role;

public class MemberBuilder {
    public static Member member() {
        return Member.builder()
                .name(NAME)
                .email(EMAIL)
                .profile(PROFILE)
                .memberKey(MEMBER_KEY)
                .role(Role.USER)
                .build();
    }
}
