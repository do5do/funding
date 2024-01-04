package com.zerobase.funding.domain.fundingproduct.repository;

import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CustomFundingProductRepository {

    Slice<FundingProduct> findFundingProducts(Pageable pageable, SearchCondition searchCondition);

    Optional<FundingProduct> findByIdFetchMember(Long id);

    Optional<FundingProduct> findByIdFetchReward(Long id);
}
