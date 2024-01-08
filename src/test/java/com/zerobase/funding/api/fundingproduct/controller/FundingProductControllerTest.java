package com.zerobase.funding.api.fundingproduct.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.funding.api.auth.jwt.TokenProvider;
import com.zerobase.funding.api.fundingproduct.dto.DetailResponse;
import com.zerobase.funding.api.fundingproduct.dto.Edit;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.dto.model.RewardDto;
import com.zerobase.funding.api.fundingproduct.service.FundingProductService;
import jakarta.validation.ConstraintViolationException;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    static final String uri = "/funding-products";
    static final String memberKey = "key";
    String title = "title";
    String description = "desc";
    LocalDate startDate = LocalDate.of(2023, 12, 8);
    LocalDate endDate = LocalDate.of(2024, 1, 8);
    Integer targetAmount = 500000;
    Integer rewardPrice = 35000;
    Integer stockQuantity = 100;

    FundingProductDto fundingProductDto = FundingProductDto.builder()
            .title(title)
            .description(description)
            .startDate(startDate)
            .endDate(endDate)
            .targetAmount(targetAmount)
            .views(0)
            .rewards(List.of(RewardDto.builder().build()))
            .build();

    @Test
    @DisplayName("펀딩 상품 목록 조회")
    void fundingProducts() throws Exception {
        // given
        given(fundingProductService.fundingProducts(any(), any()))
                .willReturn(new SliceImpl<>(List.of(fundingProductDto)));

        // when
        // then
        mockMvc.perform(get(uri)
                        .param("filterType", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(title))
                .andExpect(jsonPath("$.content[0].description").value(description))
                .andExpect(jsonPath("$.content[0].startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.content[0].endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.content[0].targetAmount").value(targetAmount))
                .andDo(print());
    }

    @Nested
    @DisplayName("펀딩 상품 등록 api")
    @WithMockUser(username = memberKey)
    class RegistrationApi {

        RewardDto rewardDto = RewardDto.builder()
                .title(title)
                .description(description)
                .price(rewardPrice)
                .stockQuantity(stockQuantity)
                .build();

        RegistrationRequest request = RegistrationRequest.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .targetAmount(targetAmount)
                .rewards(List.of(rewardDto))
                .build();

        MockMultipartFile json;
        MockMultipartFile thumbnail;
        MockMultipartFile details;

        @BeforeEach
        void setup() throws Exception {
            json = new MockMultipartFile("request", "jsondata",
                    "application/json", objectMapper.writeValueAsBytes(request));

            thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                    "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

            details = new MockMultipartFile("details", "java.png",
                    "png", new FileInputStream("src/test/resources/img/java.png"));
        }

        @Test
        @DisplayName("성공")
        void registration() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .file(thumbnail)
                            .file(details)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 값, 리워드 없음")
        void registration_validate_rewards_not_empty() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            RegistrationRequest request = RegistrationRequest.builder()
                    .title(title)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .targetAmount(targetAmount)
                    .rewards(List.of())
                    .build();

            MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                    "application/json", objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 값, 리워드의 title 누락")
        void registration_validate_rewards_title_not_blank() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            RegistrationRequest request = RegistrationRequest.builder()
                    .title(title)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .targetAmount(targetAmount)
                    .rewards(List.of(RewardDto.builder()
                            .description(description)
                            .price(rewardPrice)
                            .stockQuantity(stockQuantity)
                            .build()))
                    .build();

            MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                    "application/json", objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 값, 펀딩 시작 날짜 누락")
        void registration_validate_start_date_not_null() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            RegistrationRequest request = RegistrationRequest.builder()
                    .title(title)
                    .description(description)
                    .endDate(endDate)
                    .targetAmount(targetAmount)
                    .rewards(List.of(RewardDto.builder()
                            .title(title)
                            .description(description)
                            .price(rewardPrice)
                            .stockQuantity(stockQuantity)
                            .build()))
                    .build();

            MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                    "application/json", objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 값, 목표 금액 누락")
        void registration_validate_target_amount_not_null() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            RegistrationRequest request = RegistrationRequest.builder()
                    .title(title)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .rewards(List.of(RewardDto.builder()
                            .title(title)
                            .description(description)
                            .price(rewardPrice)
                            .stockQuantity(stockQuantity)
                            .build()))
                    .build();

            MockMultipartFile json = new MockMultipartFile("request", "jsondata",
                    "application/json", objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 파일")
        void registration_invalid_file() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            MockMultipartFile invalidThumbnail = new MockMultipartFile("thumbnail",
                    "invalidFile.json",
                    "application/json",
                    new FileInputStream("src/test/resources/img/invalidFile.json"));

            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .file(invalidThumbnail)
                            .file(details)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(result -> assertTrue(result.getResolvedException()
                            instanceof ConstraintViolationException))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 파일 개수 초과 (max 5)")
        void registration_invalid_file_list() throws Exception {
            // given
            given(fundingProductService.registration(any(), any(), any(), any()))
                    .willReturn(fundingProductDto);

            // when
            // then
            mockMvc.perform(multipart(HttpMethod.POST, uri)
                            .file(json)
                            .file(thumbnail)
                            .file(details)
                            .file(details)
                            .file(details)
                            .file(details)
                            .file(details)
                            .file(details)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(result -> assertTrue(result.getResolvedException()
                            instanceof ConstraintViolationException))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("펀딩 상품 상세")
    void detail() throws Exception {
        // given
        given(fundingProductService.detail(any()))
                .willReturn(DetailResponse.builder()
                        .remainingDays(12)
                        .totalAmount(20000)
                        .completionPercent(4)
                        .donorCount(2)
                        .build());

        // when
        // then
        mockMvc.perform(get(uri + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingDays").value(12))
                .andExpect(jsonPath("$.totalAmount").value(20000))
                .andExpect(jsonPath("$.completionPercent").value(4))
                .andExpect(jsonPath("$.donorCount").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("펀딩 상품 수정")
    @WithMockUser(username = memberKey)
    void edit() throws Exception {
        // given
        given(fundingProductService.edit(any(), any(), any()))
                .willReturn(Edit.Response.builder()
                        .title(title)
                        .description(description)
                        .startDate(startDate)
                        .endDate(endDate)
                        .targetAmount(targetAmount)
                        .build());

        // when
        // then
        mockMvc.perform(patch(uri + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Edit.Request.builder()
                        .title(title)
                        .description(description)
                        .startDate(startDate)
                        .endDate(endDate)
                        .targetAmount(targetAmount)
                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.targetAmount").value(targetAmount))
                .andDo(print());
    }

    @Test
    @DisplayName("펀딩 상품 삭제")
    @WithMockUser(username = memberKey)
    void deleteFunding() throws Exception {
        // given
        doNothing().when(fundingProductService).delete(any(), any());

        // when
        // then
        mockMvc.perform(delete(uri + "/1"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}