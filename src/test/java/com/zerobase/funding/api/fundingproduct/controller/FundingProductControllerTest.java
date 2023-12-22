package com.zerobase.funding.api.fundingproduct.controller;

import static com.zerobase.funding.common.constants.FundingProductConstants.DESCRIPTION;
import static com.zerobase.funding.common.constants.FundingProductConstants.END_DATE;
import static com.zerobase.funding.common.constants.FundingProductConstants.START_DATE;
import static com.zerobase.funding.common.constants.FundingProductConstants.TARGET_AMOUNT;
import static com.zerobase.funding.common.constants.FundingProductConstants.TITLE;
import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.dto.model.RewardDto;
import com.zerobase.funding.api.fundingproduct.service.FundingProductService;
import com.zerobase.funding.common.constants.RewardConstants;
import java.io.FileInputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(FundingProductController.class)
class FundingProductControllerTest {

    @MockBean
    FundingProductService fundingProductService;

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

    @Test
    @DisplayName("펀딩 상품 목록 조회")
    void fundingProducts() throws Exception {
        // given
        given(fundingProductService.fundingProducts(any(), any()))
                .willReturn(new SliceImpl<>(List.of(getFundingProductDto())));

        // when
        // then
        mockMvc.perform(get("/funding-products")
                        .param("filterType", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(TITLE))
                .andExpect(jsonPath("$.content[0].description").value(DESCRIPTION))
                .andExpect(jsonPath("$.content[0].startDate").value(START_DATE.toString()))
                .andExpect(jsonPath("$.content[0].endDate").value(END_DATE.toString()))
                .andExpect(jsonPath("$.content[0].targetAmount").value(TARGET_AMOUNT))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("펀딩 상품 등록")
    void registration() throws Exception {
        // given
        given(fundingProductService.registration(any(), any(), any(), any()))
                .willReturn(1L);

        // when
        // then
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of(RewardDto.builder()
                        .title(RewardConstants.TITLE)
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();

        MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                "application/json", objectMapper.writeValueAsBytes(request));

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

        MockMultipartFile details = new MockMultipartFile("details", "java.png",
                "png", new FileInputStream("src/test/resources/img/java.png"));

        mockMvc.perform(multipart(HttpMethod.POST, "/funding-products")
                        .file(json)
                        .file(thumbnail)
                        .file(details)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("펀딩 상품 등록 실패 - 유효하지 않은 값, 리워드 없음")
    void registration_validate_rewards_not_empty() throws Exception {
        // given
        given(fundingProductService.registration(any(), any(), any(), any()))
                .willReturn(1L);

        // when
        // then
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of())
                .build();

        MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(HttpMethod.POST, "/funding-products")
                        .file(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("펀딩 상품 등록 실패 - 유효하지 않은 값, 리워드의 title 누락")
    void registration_validate_rewards_title_not_blank() throws Exception {
        // given
        given(fundingProductService.registration(any(), any(), any(), any()))
                .willReturn(1L);

        // when
        // then
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of(RewardDto.builder()
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();

        MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(HttpMethod.POST, "/funding-products")
                        .file(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("펀딩 상품 등록 실패 - 유효하지 않은 값, 펀딩 시작 날짜 누락")
    void registration_validate_start_date_not_null() throws Exception {
        // given
        given(fundingProductService.registration(any(), any(), any(), any()))
                .willReturn(1L);

        // when
        // then
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
//                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of(RewardDto.builder()
                        .title(RewardConstants.TITLE)
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();

        MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(HttpMethod.POST, "/funding-products")
                        .file(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = MEMBER_KEY)
    @DisplayName("펀딩 상품 등록 실패 - 유효하지 않은 값, 목표 금액 누락")
    void registration_validate_target_amount_not_null() throws Exception {
        // given
        given(fundingProductService.registration(any(), any(), any(), any()))
                .willReturn(1L);

        // when
        // then
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .rewards(List.of(RewardDto.builder()
                        .title(RewardConstants.TITLE)
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();

        MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(HttpMethod.POST, "/funding-products")
                        .file(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    private static FundingProductDto getFundingProductDto() {
        return FundingProductDto.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .views(0)
                .rewards(List.of(RewardDto.builder().build()))
                .build();
    }
}