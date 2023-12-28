package com.zerobase.funding.api.auth.controller;

import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.api.auth.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @MockBean
    TokenService tokenService;

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
        doNothing().when(tokenService).deleteRefreshToken(any());

        // when
        // then
        mockMvc.perform(delete("/auth/logout"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}