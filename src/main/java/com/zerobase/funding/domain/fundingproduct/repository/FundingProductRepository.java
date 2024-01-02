package com.zerobase.funding.domain.fundingproduct.repository;

import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingProductRepository extends JpaRepository<FundingProduct, Long>,
        CustomFundingProductRepository {

    List<FundingProduct> findByIdIn(Set<Long> fundingProductIds);

    Optional<FundingProduct> findByIdAndDeleted(Long id, boolean isDelete);
}
