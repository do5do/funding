package com.zerobase.funding.api.fundingproduct.service;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.entity.Image;
import com.zerobase.funding.domain.fundingproduct.entity.ImageType;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.api.s3.AwsS3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FundingProductService {

    private final FundingProductRepository fundingProductRepository;
    private final AwsS3Service awsS3Service;
    private final AuthenticationService authenticationService;

    public Slice<FundingProductDto> fundingProducts(Pageable pageable,
            SearchCondition searchCondition) {
        return fundingProductRepository.findFundingProducts(pageable, searchCondition)
                .map(FundingProductDto::fromEntity);
    }

    @Transactional
    public Long registration(RegistrationRequest request, MultipartFile thumbnail,
            List<MultipartFile> details, String memberKey) {
        Member member = authenticationService.getMemberOrThrow(memberKey);

        String thumbnailUrl = awsS3Service.uploadFile(thumbnail);
        List<String> detailUrls = awsS3Service.uploadFiles(details);

        FundingProduct fundingProduct = request.toEntity();
        fundingProduct.addMember(member);

        fundingProduct.addImages(new Image(ImageType.THUMBNAIL, thumbnailUrl));
        detailUrls.forEach(img -> fundingProduct.addImages(new Image(ImageType.DETAIL, img)));

        request.rewards().forEach(o -> fundingProduct.addRewards(o.toEntity()));

        fundingProductRepository.save(fundingProduct);
        return fundingProduct.getId();
    }
}
