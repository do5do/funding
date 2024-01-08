package com.zerobase.funding.domain.fundingproduct.repository;

import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingProductRepository extends JpaRepository<FundingProduct, Long>,
        CustomFundingProductRepository {

    Optional<FundingProduct> findByIdAndDeleted(Long id, boolean isDelete);
}
