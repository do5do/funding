package com.zerobase.funding.domain.funding.repository.impl;

import static com.zerobase.funding.domain.delivery.entity.QDelivery.delivery;
import static com.zerobase.funding.domain.funding.entity.QFunding.funding;
import static com.zerobase.funding.domain.member.entity.QMember.member;
import static com.zerobase.funding.domain.reward.entity.QReward.reward;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.entity.Status;
import com.zerobase.funding.domain.funding.repository.CustomFundingRepository;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.time.LocalDateTime;
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
                .where(funding.reward.in(rewards), statusEq(status))
                .fetch();
    }

    @Override
    public List<Funding> findAllByMemberPerMonthFetch(Member member,
            LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return queryFactory.selectFrom(funding)
                .join(funding.reward, reward).fetchJoin()
                .where(funding.member.eq(member),
                        funding.createdDate.between(startDateTime, endDateTime))
                .fetch();
    }

    private BooleanExpression statusEq(Status status) {
        return status != null ? funding.status.eq(status) : null;
    }
}
