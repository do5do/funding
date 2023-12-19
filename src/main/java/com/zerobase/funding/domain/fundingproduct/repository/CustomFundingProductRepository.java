package com.zerobase.funding.domain.fundingproduct.repository;

import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CustomFundingProductRepository {

    Slice<FundingProduct> findFundingProducts(Pageable pageable, FilterType filterType);
}
