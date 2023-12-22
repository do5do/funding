package com.zerobase.funding.api.member.service;

import static com.zerobase.funding.global.exception.ErrorCode.MEMBER_NOT_FOUND;

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

    public MemberDto memberInfo(String memberKey) {
        return MemberDto.fromEntity(findByMemberKeyOrThrow(memberKey));
    }

    @Transactional
    public MemberDto memberEdit(MemberEditRequest request, String memberKey) {
        Member member = findByMemberKeyOrThrow(memberKey);
        member.updateMember(request);
        return MemberDto.fromEntity(member);
    }

    private Member findByMemberKeyOrThrow(String memberKey) {
        return memberRepository.findByMemberKey(memberKey)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }
}
