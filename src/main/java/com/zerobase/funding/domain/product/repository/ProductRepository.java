package com.zerobase.funding.domain.product.repository;

import com.zerobase.funding.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
