package com.zerobase.funding.api.funding.service;

import static com.zerobase.funding.api.exception.ErrorCode.ADDRESS_IS_REQUIRED;
import static com.zerobase.funding.api.exception.ErrorCode.ALREADY_FUNDED_REWARD;
import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.OUT_OF_STOCK;
import static com.zerobase.funding.api.exception.ErrorCode.REWARD_NOT_MATCH;
import static com.zerobase.funding.domain.delivery.entity.Status.WAITING;
import static com.zerobase.funding.domain.funding.entity.Status.IN_PROGRESS;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.common.constants.RedisKey;
import com.zerobase.funding.api.funding.dto.CreateFunding;
import com.zerobase.funding.api.funding.dto.PredicatedResponse;
import com.zerobase.funding.api.funding.exception.FundingException;
import com.zerobase.funding.api.lock.annotation.DistributedLock;
import com.zerobase.funding.api.member.dto.model.AddressDto;
import com.zerobase.funding.api.reward.service.RewardService;
import com.zerobase.funding.domain.delivery.entity.Delivery;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.repository.FundingRepository;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FundingService {

    private final FundingRepository fundingRepository;
    private final AuthenticationService authenticationService;
    private final RewardService rewardService;

    public List<Funding> getFundingByRewards(List<Reward> rewards) {
        return fundingRepository.findAllByRewardInAndStatus(rewards, IN_PROGRESS);
    }

    public Funding getFundingById(Long id) {
        return fundingRepository.findById(id)
                .orElseThrow(() -> new FundingException(FUNDING_NOT_FOUND));
    }

    @DistributedLock(keyPrefix = RedisKey.FUNDING_LOCK_PREFIX, idField = "rewardId")
    public CreateFunding.Response createFunding(CreateFunding.Request request,
            String memberKey) {
        Member member = authenticationService.getMemberOrThrow(memberKey);
        Reward reward = rewardService.getRewardOrThrow(request.rewardId());

        validateAddAddress(request, member);
        validateReward(request, reward);
        validateFunding(member, reward);

        reward.decreaseStockQuantity();

        Funding funding = request.toEntity(reward.getPrice());
        funding.addMember(member);
        funding.addReward(reward);
        funding.addDelivery(Delivery.of(WAITING));

        fundingRepository.save(funding);

        return CreateFunding.Response.from(
                reward.getTitle(), reward.getPrice(), funding.getId());
    }

    private void validateFunding(Member member, Reward reward) {
        if (fundingRepository.existsByMemberAndReward(member, reward)) {
            throw new FundingException(ALREADY_FUNDED_REWARD);
        }
    }

    private void validateAddAddress(CreateFunding.Request request, Member member) {
        AddressDto requestAddress = request.address();

        if (member.getAddress() == null && requestAddress == null) {
            throw new FundingException(ADDRESS_IS_REQUIRED);
        }

        if (requestAddress != null) {
            member.addAddress(requestAddress.toEntity());
        }
    }

    private void validateReward(CreateFunding.Request request, Reward reward) {
        if (!Objects.equals(request.fundingProductId(), reward.getFundingProduct().getId())) {
            throw new FundingException(REWARD_NOT_MATCH);
        }

        if (reward.getStockQuantity() < 1) {
            throw new FundingException(OUT_OF_STOCK);
        }
    }

    public List<PredicatedResponse> predicatedFundingsPerMonth(YearMonth yearMonth,
            String memberKey) {
        Member member = authenticationService.getMemberOrThrow(memberKey);
        LocalDateTime startDateTime = LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(),
                1, 0, 0);

        return fundingRepository.findAllByMemberPerMonthFetch(member,
                        startDateTime, startDateTime.plusMonths(1)).stream()
                .map(PredicatedResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
