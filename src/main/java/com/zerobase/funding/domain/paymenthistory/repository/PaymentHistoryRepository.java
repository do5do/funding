package com.zerobase.funding.domain.paymenthistory.repository;

import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.paymenthistory.entity.PaymentHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    Optional<PaymentHistory> findByFunding(Funding funding);
}
