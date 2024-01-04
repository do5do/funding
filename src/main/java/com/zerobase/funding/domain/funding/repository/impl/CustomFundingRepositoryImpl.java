package com.zerobase.funding.domain.funding.repository.impl;

import static com.zerobase.funding.domain.delivery.entity.QDelivery.delivery;
import static com.zerobase.funding.domain.funding.entity.QFunding.funding;
import static com.zerobase.funding.domain.member.entity.QMember.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.funding.repository.CustomFundingRepository;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomFundingRepositoryImpl implements CustomFundingRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Funding> findAllByRewardInAndStatusFetch(List<Reward> rewards, Status status) {
        return queryFactory.selectFrom(funding)
                .join(funding.member, member).fetchJoin()
                .join(funding.delivery, delivery).fetchJoin()
                .where(funding.reward.in(rewards),
                        funding.status.eq(Status.IN_PROGRESS))
                .fetch();
    }

    @Override
    public List<Funding> findAllByRewardInFetch(List<Reward> rewards) {
        return queryFactory.selectFrom(funding)
                .join(funding.member, member).fetchJoin()
                .join(funding.delivery, delivery).fetchJoin()
                .where(funding.reward.in(rewards))
                .fetch();
    }
}
