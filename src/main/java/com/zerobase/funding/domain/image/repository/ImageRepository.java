package com.zerobase.funding.domain.image.repository;

import com.zerobase.funding.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
