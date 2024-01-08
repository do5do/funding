package com.zerobase.funding.api.funding.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.api.funding.dto.CreateFunding;
import com.zerobase.funding.api.funding.dto.CreateFunding.Response;
import com.zerobase.funding.api.funding.dto.PredicatedResponse;
import com.zerobase.funding.api.funding.service.FundingService;
import com.zerobase.funding.domain.funding.entity.Status;
import java.util.List;
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

@WebMvcTest(FundingController.class)
@WithMockUser(username = "memberKey")
class FundingControllerTest {

    @MockBean
    FundingService fundingService;

    @MockBean
    TokenProvider tokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    static final String uri = "/funding";
    String rewardTitle = "title";
    Integer price = 10000;
    Long id = 1L;

    @Test
    @DisplayName("펀딩하기")
    void createFunding() throws Exception {
        // given
        given(fundingService.createFunding(any(), any()))
                .willReturn(Response.builder()
                        .rewardTitle(rewardTitle)
                        .price(price)
                        .fundingId(id)
                        .build());

        // when
        // then
        mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateFunding.Request(id, id, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rewardTitle").value(rewardTitle))
                .andExpect(jsonPath("$.price").value(price))
                .andExpect(jsonPath("$.fundingId").value(id))
                .andDo(print());
    }

    @Test
    @DisplayName("펀딩 목록 기간별 조회")
    void predicateFundingsPerMonth() throws Exception {
        // given
        given(fundingService.predicatedFundingsPerMonth(any(), any()))
                .willReturn(List.of(PredicatedResponse.builder()
                        .fundingPrice(price)
                        .status(Status.IN_PROGRESS)
                        .rewardId(id)
                        .rewardTitle(rewardTitle)
                        .build()));

        // when
        // then
        mockMvc.perform(get(uri)
                        .param("yearMonth", "2024-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fundingPrice").value(price))
                .andExpect(jsonPath("$[0].status")
                        .value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].rewardId").value(id))
                .andExpect(jsonPath("$[0].rewardTitle")
                        .value(rewardTitle))
                .andDo(print());
    }
}