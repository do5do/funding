package com.zerobase.funding.domain.funding.repository;

import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    List<Funding> findByRewardInAndStatus(List<Reward> rewards, Status status);

    boolean existsByMemberAndReward(Member member, Reward reward);
}
