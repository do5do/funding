package com.zerobase.funding.api.fundingproduct.dto.model;

import com.zerobase.funding.domain.fundingproduct.entity.Image;
import com.zerobase.funding.domain.fundingproduct.entity.ImageType;

public record ImageDto(
        ImageType imageType,
        String url
) {

    public static ImageDto fromEntity(Image image) {
        return new ImageDto(image.getImageType(), image.getUrl());
    }
}
