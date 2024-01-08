package com.zerobase.funding.api.funding.service;

import static com.zerobase.funding.api.exception.ErrorCode.ADDRESS_IS_REQUIRED;
import static com.zerobase.funding.api.exception.ErrorCode.ALREADY_FUNDED_REWARD;
import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.OUT_OF_STOCK;
import static com.zerobase.funding.api.exception.ErrorCode.REWARD_NOT_MATCH;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.funding.dto.CreateFunding.Request;
import com.zerobase.funding.api.funding.dto.CreateFunding.Response;
import com.zerobase.funding.api.funding.dto.PredicatedResponse;
import com.zerobase.funding.api.funding.exception.FundingException;
import com.zerobase.funding.api.member.dto.model.AddressDto;
import com.zerobase.funding.api.reward.service.RewardService;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.funding.repository.FundingRepository;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FundingServiceTest {

    @Mock
    FundingRepository fundingRepository;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    RewardService rewardService;

    @InjectMocks
    FundingService fundingService;

    String rewardTitle = "title";
    Integer price = 10000;
    String memberKey = "key";

    Member member = Member.builder()
            .memberKey(memberKey)
            .build();
    Reward reward = Reward.builder()
            .title(rewardTitle)
            .price(price)
            .stockQuantity(1)
            .build();
    Funding funding = Funding.builder()
            .fundingPrice(price)
            .status(Status.IN_PROGRESS)
            .build();

    @Nested
    @DisplayName("펀딩하기 메소드")
    class CreateFundingMethod {

        AddressDto addressDto = AddressDto.builder()
                .roadAddress("road")
                .addressDetail("detail")
                .zipcode("1234-56")
                .build();

        @BeforeEach
        void setup() {
            reward.setFundingProduct(FundingProduct.builder()
                    .id(1L)
                    .build());
        }

        @Test
        @DisplayName("성공")
        void createFunding_success() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(rewardService.getRewardOrThrow(any()))
                    .willReturn(reward);

            given(fundingRepository.existsByMemberAndReward(any(), any()))
                    .willReturn(false);

            given(fundingRepository.save(any()))
                    .willReturn(funding);

            // when
            Request request = new Request(1L, 1L, addressDto);

            Response response =
                    fundingService.createFunding(request, memberKey);

            // then
            assertEquals(price, response.price());
            assertEquals(rewardTitle, response.rewardTitle());
            assertEquals(reward.getStockQuantity(), 0);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원인 경우 조회할 수 없다.")
        void createFunding_member_not_found() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willThrow(new AuthException(MEMBER_NOT_FOUND));

            // when
            // then
            Request request = new Request(1L, 1L, addressDto);

            assertThatThrownBy(() -> fundingService.createFunding(request, memberKey))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패 - 상품과 리워드가 연관되어야 한다.")
        void createFunding_reward_not_match() {
            // given
            reward.setFundingProduct(FundingProduct.builder()
                    .id(2L)
                    .build());

            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(rewardService.getRewardOrThrow(any()))
                    .willReturn(reward);

            // when
            // then
            Request request = new Request(1L, 1L, addressDto);

            assertThatThrownBy(() ->
                    fundingService.createFunding(request, memberKey))
                    .isInstanceOf(FundingException.class)
                    .hasMessageContaining(REWARD_NOT_MATCH.getMessage());
        }

        @Test
        @DisplayName("실패 - 리워드의 재고가 하나 이상 있어야 한다.")
        void createFunding_out_of_stock() {
            // given
            Reward reward = Reward.builder()
                    .title(rewardTitle)
                    .price(price)
                    .stockQuantity(0)
                    .build();

            reward.setFundingProduct(FundingProduct.builder()
                    .id(1L)
                    .build());

            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(rewardService.getRewardOrThrow(any()))
                    .willReturn(reward);

            // when
            // then
            Request request = new Request(1L, 1L, addressDto);

            assertThatThrownBy(() ->
                    fundingService.createFunding(request, memberKey))
                    .isInstanceOf(FundingException.class)
                    .hasMessageContaining(OUT_OF_STOCK.getMessage());
        }

        @Test
        @DisplayName("실패 - 주소는 필수 값이다.")
        void createFunding_address_is_required() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(rewardService.getRewardOrThrow(any()))
                    .willReturn(reward);

            // when
            // then
            Request request = new Request(1L, 1L, null);
            assertThatThrownBy(() ->
                    fundingService.createFunding(request, memberKey))
                    .isInstanceOf(FundingException.class)
                    .hasMessageContaining(ADDRESS_IS_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("실패 - 이미 펀딩한 리워드는 중복 펀딩할 수 없다.")
        void createFunding_already_funded_reward() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(rewardService.getRewardOrThrow(any()))
                    .willReturn(reward);

            given(fundingRepository.existsByMemberAndReward(any(), any()))
                    .willReturn(true);

            // when
            // then
            Request request = new Request(1L, 1L, addressDto);
            assertThatThrownBy(() ->
                    fundingService.createFunding(request, memberKey))
                    .isInstanceOf(FundingException.class)
                    .hasMessageContaining(ALREADY_FUNDED_REWARD.getMessage());
        }
    }

    @Nested
    @DisplayName("참여한 펀딩 목록 메소드")
    class PredicatedFundingMethod {

        @Test
        @DisplayName("성공")
        void predicatedFunding_success() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            funding.addReward(reward);
            given(fundingRepository.findAllByMemberPerMonthFetch(any(), any(), any()))
                    .willReturn(List.of(funding));

            // when
            List<PredicatedResponse> response = fundingService.predicatedFundingsPerMonth(
                    YearMonth.of(2023, 1),
                    memberKey);

            // then
            assertEquals(1, response.size());
            assertEquals(rewardTitle, response.get(0).rewardTitle());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원인 경우 조회할 수 없다.")
        void predicatedFunding_member_not_found() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willThrow(new AuthException(MEMBER_NOT_FOUND));

            // when
            // then
            assertThatThrownBy(() -> fundingService.predicatedFundingsPerMonth(
                    YearMonth.of(2023, 1), memberKey))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
        }
    }
}