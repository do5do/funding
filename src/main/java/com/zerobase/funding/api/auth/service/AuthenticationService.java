package com.zerobase.funding.api.auth.service;

import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.NO_ACCESS;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final MemberRepository memberRepository;

    public Member getMemberOrThrow(String memberKey) {
        return memberRepository.findByMemberKey(memberKey)
                .orElseThrow(() -> new AuthException(MEMBER_NOT_FOUND));
    }

    public void checkAccess(String memberKey, Member member) {
        if (!member.getMemberKey().equals(memberKey)) {
            throw new AuthException(NO_ACCESS);
        }
    }

    public void existsMemberOrThrow(String memberKey) {
       if (!memberRepository.existsByMemberKey(memberKey)) {
           throw new AuthException(MEMBER_NOT_FOUND);
       }
    }
}
