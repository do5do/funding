package com.zerobase.funding.domain.paymenthistory.repository;

import com.zerobase.funding.domain.paymenthistory.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

}
