package com.zerobase.funding.domain.reward.repository;

import com.zerobase.funding.domain.reward.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, Long> {

}
