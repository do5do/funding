package com.zerobase.funding.api.reward.service;

import static com.zerobase.funding.api.exception.ErrorCode.REWARD_NOT_FOUND;

import com.zerobase.funding.api.reward.exception.RewardException;
import com.zerobase.funding.domain.reward.entity.Reward;
import com.zerobase.funding.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RewardService {

    private final RewardRepository rewardRepository;

    public Reward getRewardOrThrow(Long id) {
        return rewardRepository.findByIdFetch(id)
                .orElseThrow(() -> new RewardException(REWARD_NOT_FOUND));
    }
}
