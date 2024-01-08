package com.zerobase.funding.domain.funding.repository;

import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomFundingRepository {

    List<Funding> findAllByRewardInAndStatusFetch(List<Reward> rewards, Status status);

    List<Funding> findAllByMemberPerMonthFetch(Member member, LocalDateTime startDateTime,
            LocalDateTime endDateTime);
}
