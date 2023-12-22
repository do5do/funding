package com.zerobase.funding.api.member.service;

import static com.zerobase.funding.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zerobase.funding.common.constants.MemberConstants.EMAIL;
import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static com.zerobase.funding.common.constants.MemberConstants.NAME;
import static com.zerobase.funding.common.constants.MemberConstants.PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.zerobase.funding.api.member.dto.MemberEditRequest;
import com.zerobase.funding.api.member.dto.model.MemberDto;
import com.zerobase.funding.api.member.exception.MemberException;
import com.zerobase.funding.common.builder.MemberBuilder;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원 정보 조회 성공")
    void memberInfo() {
        // given
        given(memberRepository.findByMemberKey(any()))
                .willReturn(Optional.of(MemberBuilder.member()));

        // when
        MemberDto memberDto = memberService.memberInfo(MEMBER_KEY);

        // then
        assertEquals(NAME, memberDto.getName());
        assertEquals(EMAIL, memberDto.getEmail());
        assertEquals(PROFILE, memberDto.getProfile());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 없는 회원")
    void memberInfo_member_not_found() {
        // given
        given(memberRepository.findByMemberKey(any()))
                .willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.memberInfo(MEMBER_KEY));

        // then
        assertEquals(MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void memberEdit() {
        // given
        given(memberRepository.findByMemberKey(any()))
                .willReturn(Optional.of(MemberBuilder.member()));

        // when
        String requestName = "dohee";
        MemberDto memberDto = memberService.memberEdit(
                new MemberEditRequest(requestName, null), MEMBER_KEY);

        // then
        assertEquals(requestName, memberDto.getName());
        assertEquals(EMAIL, memberDto.getEmail());
        assertEquals(PROFILE, memberDto.getProfile());
    }

    @Test
    void memberEdit_member_not_found() {
        // given
        given(memberRepository.findByMemberKey(any()))
                .willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.memberEdit(
                        new MemberEditRequest("dohee", null), MEMBER_KEY));

        // then
        assertEquals(MEMBER_NOT_FOUND, exception.getErrorCode());
    }
}