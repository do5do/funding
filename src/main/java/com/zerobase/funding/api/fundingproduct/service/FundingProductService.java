package com.zerobase.funding.api.fundingproduct.service;

import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FundingProductService {
    private final FundingProductRepository fundingProductRepository;

    public Slice<FundingProductDto> fundingProducts(Pageable pageable, FilterType filterType) {
        return fundingProductRepository.findFundingProducts(pageable, filterType)
                .map(FundingProductDto::fromEntity);
    }
}
