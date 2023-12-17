package com.zerobase.funding.api.auth.controller;

import static com.zerobase.funding.api.exception.ErrorCode.TOKEN_NOT_FOUND;
import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.api.auth.service.RefreshTokenService;
import com.zerobase.funding.domain.redis.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(AuthController.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class AuthControllerTest {

    @MockBean
    RefreshTokenService refreshTokenService;

    @MockBean
    TokenProvider tokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("로그아웃 처리 - refresh 토큰 삭제")
    void logout() throws Exception {
        // given
        given(refreshTokenService.findByIdOrThrow(any()))
                .willReturn(new RefreshToken(MEMBER_KEY, "refresh", "access"));

        doNothing().when(refreshTokenService).deleteRefreshToken(any());

        // when
        // then
        mockMvc.perform(delete("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberKey").value(MEMBER_KEY))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("로그아웃 처리 - 삭제할 refresh 토큰이 없는 경우")
    void logout_token_not_found() throws Exception {
        // given
        given(refreshTokenService.findByIdOrThrow(any()))
                .willThrow(new AuthException(TOKEN_NOT_FOUND));

        // when
        // then
        mockMvc.perform(delete("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberKey").value(MEMBER_KEY))
                .andDo(print());
    }
}