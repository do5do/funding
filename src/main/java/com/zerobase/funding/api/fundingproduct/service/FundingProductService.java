package com.zerobase.funding.api.fundingproduct.service;

import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_ALREADY_DELETED;
import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_EDIT;
import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;
import static com.zerobase.funding.api.exception.ErrorCode.INVALID_DATE;

import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.funding.service.FundingService;
import com.zerobase.funding.api.fundingproduct.dto.DetailResponse;
import com.zerobase.funding.api.fundingproduct.dto.Edit;
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
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        validateDate(request);
        FundingProduct fundingProduct = uploadImageToS3(request, thumbnail, details);

        try {
            fundingProduct.addMember(member);
            request.rewards().forEach(o -> fundingProduct.addRewards(o.toEntity()));
            fundingProduct.setViews(0);

            fundingProductRepository.save(fundingProduct);
            return FundingProductDto.fromEntity(fundingProduct, fundingProduct.getViews());
        } catch (Exception e) {
            log.error("Exception is occurred. ", e);
            deleteImageFromS3(fundingProduct.getImages());
            throw new FundingProductException(INTERNAL_ERROR);
        }
    }

    private void validateDate(RegistrationRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new FundingProductException(INVALID_DATE);
        }
    }

    private FundingProduct uploadImageToS3(RegistrationRequest request,
            MultipartFile thumbnail, List<MultipartFile> details) {
        FundingProduct fundingProduct = request.toEntity();

        Image thumbnailImage = awsS3Service.uploadFile(thumbnail)
                .thenApply(s3FileDto -> new Image(ImageType.THUMBNAIL, s3FileDto.url(),
                        s3FileDto.filename())).join();

        List<CompletableFuture<S3FileDto>> futures = details.stream()
                .map(awsS3Service::uploadFile)
                .toList();

        // 모든 detail image 비동기 처리 완료 후 후작업
        List<Image> detailImages = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]))
                .thenApply(Void -> futures.stream()
                        .map(CompletableFuture::join)
                        .map(s3FileDto -> new Image(ImageType.DETAIL, s3FileDto.url(),
                                s3FileDto.filename()))
                        .toList())
                .join();

        fundingProduct.addImages(thumbnailImage);
        detailImages.forEach(fundingProduct::addImages);

        return fundingProduct;
    }

    public DetailResponse detail(Long id) {
        FundingProduct fundingProduct = fundingProductRepository.findByIdAndDeleted(id, false)
                .orElseThrow(() -> new FundingProductException(FUNDING_PRODUCT_NOT_FOUND));

        List<Funding> fundingList = fundingService.getFundingByRewards(fundingProduct.getRewards());

        Integer views = viewsService.saveOrUpdate(String.valueOf(id), fundingProduct.getViews());

        return DetailResponse.fromEntity(fundingProduct, fundingList, views);
    }

    @Transactional
    public Edit.Response edit(Long id, Edit.Request request, String memberKey) {
        FundingProduct fundingProduct = fundingProductRepository.findByIdFetch(id)
                .orElseThrow(() -> new FundingProductException(FUNDING_PRODUCT_NOT_FOUND));

        authenticationService.checkAccess(memberKey, fundingProduct.getMember());

        validateFundingProduct(fundingProduct);

        fundingProduct.updateFundingProduct(request);
        return Edit.Response.fromEntity(fundingProduct);
    }

    @Transactional
    public void delete(Long id, String memberKey) {
        FundingProduct fundingProduct = fundingProductRepository.findByIdFetch(id)
                .orElseThrow(() -> new FundingProductException(FUNDING_PRODUCT_NOT_FOUND));

        authenticationService.checkAccess(memberKey, fundingProduct.getMember());

        validateFundingProduct(fundingProduct);

        viewsService.deleteViews(String.valueOf(id));
        deleteImageFromS3(fundingProduct.getImages());

        fundingProduct.setDeleted(); // 삭제 처리
    }

    private void deleteImageFromS3(List<Image> images) {
        images.forEach(o -> awsS3Service.deleteFile(o.getFilename()));
    }

    private void validateFundingProduct(FundingProduct fundingProduct) {
        LocalDate now = LocalDate.now();

        if (!fundingProduct.getStartDate().isAfter(now)) {
            throw new FundingProductException(FUNDING_PRODUCT_NOT_EDIT);
        }

        if (fundingProduct.isDeleted()) {
            throw new FundingProductException(FUNDING_PRODUCT_ALREADY_DELETED);
        }
    }
}
