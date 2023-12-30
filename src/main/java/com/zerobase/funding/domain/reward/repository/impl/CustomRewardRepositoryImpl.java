package com.zerobase.funding.domain.reward.repository.impl;

import static com.zerobase.funding.domain.fundingproduct.entity.QFundingProduct.fundingProduct;
import static com.zerobase.funding.domain.reward.entity.QReward.reward;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.funding.domain.reward.entity.Reward;
import com.zerobase.funding.domain.reward.repository.CustomRewardRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomRewardRepositoryImpl implements CustomRewardRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Reward> findByIdFetch(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(reward)
                .join(reward.fundingProduct, fundingProduct).fetchJoin()
                .where(reward.id.eq(id))
                .fetchOne());
    }
}
