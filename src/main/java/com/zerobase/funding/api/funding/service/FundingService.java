package com.zerobase.funding.api.funding.service;

import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.funding.repository.FundingRepository;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FundingService {

    private final FundingRepository fundingRepository;

    public List<Funding> findByRewards(List<Reward> rewards) {
        return fundingRepository.findByRewardInAndStatus(rewards, Status.IN_PROGRESS);
    }
}
