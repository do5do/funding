package com.zerobase.funding.api.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.api.member.dto.MemberEditRequest;
import com.zerobase.funding.api.member.dto.model.AddressDto;
import com.zerobase.funding.api.member.dto.model.MemberDto;
import com.zerobase.funding.api.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @MockBean
    MemberService memberService;

    @MockBean
    TokenProvider tokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    static final String memberKey = "key";
    String name = "name";
    String email = "do@gmail.com";
    String profile = "profile";

    MemberDto memberDto = MemberDto.builder()
            .name(name)
            .email(email)
            .profile(profile)
            .address(AddressDto.builder().build())
            .build();

    @Test
    @WithMockUser(username = memberKey)
    @DisplayName("회원 정보 조회")
    void memberInfo() throws Exception {
        // given
        given(memberService.memberInfo(any()))
                .willReturn(memberDto);

        // when
        // then
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.profile").value(profile))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = memberKey)
    @DisplayName("회원 정보 수정")
    void memberEdit() throws Exception {
        // given
        given(memberService.memberEdit(any(), any()))
                .willReturn(memberDto);

        // when
        // then
        mockMvc.perform(patch("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MemberEditRequest("dohee", null)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.profile").value(profile))
                .andDo(print());
    }
}