package com.zerobase.funding.domain.fundingproduct.repository.impl;

import static com.zerobase.funding.domain.fundingproduct.entity.QFundingProduct.fundingProduct;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.CustomFundingProductRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CustomFundingProductRepositoryImpl implements CustomFundingProductRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<FundingProduct> findFundingProducts(Pageable pageable, FilterType filterType) {
        List<FundingProduct> contents = queryFactory
                .selectFrom(fundingProduct)
                .where(filtering(filterType))
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (contents.size() > pageable.getPageSize()) {
            contents.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(contents, pageable, hasNext);
    }

    private OrderSpecifier[] getOrderSpecifier(Pageable pageable) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        if (!pageable.getSort().isEmpty()) {
            pageable.getSort().forEach(order -> {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                PathBuilder pathBuilder = new PathBuilder<>(
                        FundingProduct.class, "fundingProduct");
                orderSpecifiers.add(new OrderSpecifier<>(direction, pathBuilder.get(property)));
            });
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression filtering(FilterType filterType) {
        if (filterType.equals(FilterType.UPCOMING)) {
            return fundingProduct.startDate.after(LocalDate.now());
        }

        return fundingProduct.startDate.loe(LocalDate.now())
                .and(fundingProduct.endDate.goe(LocalDate.now()));
    }
}
