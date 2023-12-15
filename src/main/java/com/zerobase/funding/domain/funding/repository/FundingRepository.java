package com.zerobase.funding.domain.funding.repository;

import com.zerobase.funding.domain.funding.entity.Funding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingRepository extends JpaRepository<Funding, Long> {

}
