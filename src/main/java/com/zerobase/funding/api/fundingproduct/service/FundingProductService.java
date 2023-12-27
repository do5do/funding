package com.zerobase.funding.api.fundingproduct.service;

import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.funding.service.FundingService;
import com.zerobase.funding.api.fundingproduct.dto.DetailResponse;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.exception.FundingProductException;
import com.zerobase.funding.api.s3.AwsS3Service;
import com.zerobase.funding.api.s3.dto.S3FileDto;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.image.entity.Image;
import com.zerobase.funding.domain.image.entity.ImageType;
import com.zerobase.funding.domain.member.entity.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FundingProductService {

    private final FundingProductRepository fundingProductRepository;
    private final AwsS3Service awsS3Service;
    private final AuthenticationService authenticationService;
    private final FundingService fundingService;
    private final ViewsService viewsService;

    public Slice<FundingProductDto> fundingProducts(Pageable pageable,
            SearchCondition searchCondition) {
        return fundingProductRepository.findFundingProducts(pageable, searchCondition)
                .map(o -> {
                    Integer views = viewsService.getViews(String.valueOf(o.getId()), o.getViews());
                    return FundingProductDto.fromEntity(o, views);
                });
    }

    @Transactional
    public FundingProductDto registration(RegistrationRequest request, MultipartFile thumbnail,
            List<MultipartFile> details, String memberKey) {
        Member member = authenticationService.getMemberOrThrow(memberKey);

        S3FileDto fileThumbnail = awsS3Service.uploadFile(thumbnail);
        List<S3FileDto> fileDetails = awsS3Service.uploadFiles(details);

        try {
            FundingProduct fundingProduct = request.toEntity();
            fundingProduct.addMember(member);

            fundingProduct.addImages(new Image(ImageType.THUMBNAIL, fileThumbnail.url()));
            fileDetails.forEach(fileDto -> fundingProduct.addImages(
                    new Image(ImageType.DETAIL, fileDto.url())));

            request.rewards().forEach(o -> fundingProduct.addRewards(o.toEntity()));

            fundingProduct.setViews(0);

            fundingProductRepository.save(fundingProduct);
            return FundingProductDto.fromEntity(fundingProduct, fundingProduct.getViews());
        } catch (Exception e) {
            log.error("Exception is occurred. ", e);
            awsS3Service.deleteFile(fileThumbnail);
            awsS3Service.deleteFiles(fileDetails);
            throw new FundingProductException(INTERNAL_ERROR);
        }
    }

    public DetailResponse detail(Long id) {
        FundingProduct fundingProduct = fundingProductRepository.findById(id)
                .orElseThrow(() -> new FundingProductException(FUNDING_PRODUCT_NOT_FOUND));

        List<Funding> fundingList = fundingService.findByRewards(fundingProduct.getRewards());

        Integer views = viewsService.saveOrUpdate(String.valueOf(id), fundingProduct.getViews());

        return DetailResponse.fromEntity(fundingProduct, fundingList, views);
    }
}
