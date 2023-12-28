package com.zerobase.funding.api.member.service;

import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.member.dto.model.MemberDto;
import com.zerobase.funding.api.member.dto.MemberEditRequest;
import com.zerobase.funding.api.member.exception.MemberException;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationService authenticationService;

    public MemberDto memberInfo(String memberKey) {
        Member member = findByMemberKeyOrThrow(memberKey);
        authenticationService.checkAccess(memberKey, member);
        return MemberDto.fromEntity(member);
    }

    @Transactional
    public MemberDto memberEdit(MemberEditRequest request, String memberKey) {
        Member member = findByMemberKeyOrThrow(memberKey);
        authenticationService.checkAccess(memberKey, member);
        member.updateMember(request);
        return MemberDto.fromEntity(member);
    }

    private Member findByMemberKeyOrThrow(String memberKey) {
        return memberRepository.findByMemberKey(memberKey)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }
}
