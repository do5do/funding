package com.zerobase.funding.domain.reward.repository;

import com.zerobase.funding.domain.reward.entity.Reward;
import java.util.Optional;

public interface CustomRewardRepository {

    Optional<Reward> findByIdFetch(Long id);
}
