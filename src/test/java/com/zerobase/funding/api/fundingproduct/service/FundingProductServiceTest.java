package com.zerobase.funding.api.fundingproduct.service;

import static com.zerobase.funding.common.constants.FundingProductConstants.DESCRIPTION;
import static com.zerobase.funding.common.constants.FundingProductConstants.END_DATE;
import static com.zerobase.funding.common.constants.FundingProductConstants.START_DATE;
import static com.zerobase.funding.common.constants.FundingProductConstants.TARGET_AMOUNT;
import static com.zerobase.funding.common.constants.FundingProductConstants.TITLE;
import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.dto.model.RewardDto;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.api.s3.AwsS3Service;
import com.zerobase.funding.api.s3.dto.S3FileDto;
import com.zerobase.funding.common.builder.MemberBuilder;
import com.zerobase.funding.common.constants.RewardConstants;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.entity.Image;
import com.zerobase.funding.domain.fundingproduct.entity.ImageType;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.reward.entity.Reward;
import com.zerobase.funding.api.exception.ErrorCode;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FundingProductServiceTest {

    @Mock
    FundingProductRepository fundingProductRepository;

    @Mock
    AwsS3Service awsS3Service;

    @Mock
    AuthenticationService authenticationService;

    @InjectMocks
    FundingProductService fundingProductService;

    @Test
    @DisplayName("펀딩 상품 목록 조회 성공")
    void fundingProducts() {
        // given
        FundingProduct fundingProduct = getFundingProduct();

        Reward reward = Reward.builder()
                .title(RewardConstants.TITLE)
                .description(RewardConstants.DESCRIPTION)
                .price(RewardConstants.PRICE)
                .stockQuantity(RewardConstants.STOCK_QUANTITY)
                .build();

        fundingProduct.addMember(MemberBuilder.member());
        fundingProduct.addRewards(reward);
        fundingProduct.addImages(new Image(ImageType.THUMBNAIL, "url"));

        given(fundingProductRepository.findFundingProducts(any(), any()))
                .willReturn(new SliceImpl<>(List.of(fundingProduct)));

        // when
        Slice<FundingProductDto> fundingProducts = fundingProductService.fundingProducts(
                PageRequest.of(0, 3),
                new SearchCondition(FilterType.IN_PROGRESS, null));

        // then
        assertEquals(1, fundingProducts.getContent().size());
    }

    @Test
    @DisplayName("펀딩 상품 등록 성공")
    void registration() throws IOException {
        // given
        given(authenticationService.getMemberOrThrow(any()))
                .willReturn(MemberBuilder.member());

        given(awsS3Service.uploadFile(any()))
                .willReturn(new S3FileDto("thumbnail_url", ""));

        given(awsS3Service.uploadFiles(any()))
                .willReturn(List.of(new S3FileDto("detail_url", "")));

        given(fundingProductRepository.save(any()))
                .willReturn(getFundingProduct());

        // when
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of(RewardDto.builder()
                        .title(RewardConstants.TITLE)
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

        MockMultipartFile details = new MockMultipartFile("details", "java.png",
                "png", new FileInputStream("src/test/resources/img/java.png"));

        fundingProductService.registration(request, thumbnail, List.of(details), MEMBER_KEY);

        ArgumentCaptor<FundingProduct> captor = ArgumentCaptor.forClass(FundingProduct.class);

        // then
        verify(fundingProductRepository, times(1)).save(captor.capture());
        assertEquals(TITLE, captor.getValue().getTitle());
    }

    @Test
    @DisplayName("펀딩 상품 등록 실패 - 없는 회원")
    void registration_member_not_found() throws IOException {
        // given
        given(authenticationService.getMemberOrThrow(any()))
                .willThrow(new AuthException(ErrorCode.MEMBER_NOT_FOUND));

        // when
        RegistrationRequest request = RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of(RewardDto.builder()
                        .title(RewardConstants.TITLE)
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

        MockMultipartFile details = new MockMultipartFile("details", "java.png",
                "png", new FileInputStream("src/test/resources/img/java.png"));

        AuthException exception = assertThrows(AuthException.class, () ->
                fundingProductService.registration(request, thumbnail, List.of(details),
                        MEMBER_KEY));

        // then
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    private static FundingProduct getFundingProduct() {
        return FundingProduct.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .targetAmount(TARGET_AMOUNT)
                .build();
    }
}