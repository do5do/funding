package com.zerobase.funding.domain.funding.repository;

import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.util.List;

public interface CustomFundingRepository {

    List<Funding> findAllByRewardInAndStatusFetch(List<Reward> rewards, Status status);

    List<Funding> findAllByRewardInFetch(List<Reward> rewards);
}
