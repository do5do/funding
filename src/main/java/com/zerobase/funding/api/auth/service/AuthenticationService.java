package com.zerobase.funding.api.auth.service;

import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.zerobase.funding.api.member.exception.MemberException;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final MemberRepository memberRepository;

    public Member getMemberOrThrow(String memberKey) {
        return memberRepository.findByMemberKey(memberKey)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }
}
