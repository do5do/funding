package com.zerobase.funding.domain.delivery.repository;

import com.zerobase.funding.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

}
