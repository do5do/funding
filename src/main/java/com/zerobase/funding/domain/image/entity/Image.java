package com.zerobase.funding.domain.image.entity;

import com.zerobase.funding.domain.common.entity.BaseTimeEntity;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String filename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_product_id")
    private FundingProduct fundingProduct;

    public Image(ImageType imageType, String url, String filename) {
        this.imageType = imageType;
        this.url = url;
        this.filename = filename;
    }

    public void setFundingProduct(FundingProduct fundingProduct) {
        this.fundingProduct = fundingProduct;
    }
}
