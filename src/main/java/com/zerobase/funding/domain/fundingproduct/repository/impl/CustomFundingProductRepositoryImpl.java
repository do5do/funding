package com.zerobase.funding.domain.fundingproduct.repository.impl;

import static com.zerobase.funding.domain.fundingproduct.entity.QFundingProduct.fundingProduct;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.api.fundingproduct.type.SortType;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.CustomFundingProductRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CustomFundingProductRepositoryImpl implements CustomFundingProductRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<FundingProduct> findFundingProducts(Pageable pageable,
            SearchCondition searchCondition) {
        List<FundingProduct> contents = queryFactory
                .selectFrom(fundingProduct)
                .where(filtering(searchCondition.filterType()))
                .orderBy(ordering(searchCondition.sortType()))
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

    private OrderSpecifier<?> ordering(SortType sortType) {
        if (SortType.VIEWS.equals(sortType)) {
            return fundingProduct.views.desc();
        }
        return fundingProduct.id.desc();
    }

    private BooleanExpression filtering(FilterType filterType) {
        if (FilterType.UPCOMING.equals(filterType)) {
            return fundingProduct.startDate.after(LocalDate.now());
        }

        return fundingProduct.startDate.loe(LocalDate.now())
                .and(fundingProduct.endDate.goe(LocalDate.now()));
    }
}
