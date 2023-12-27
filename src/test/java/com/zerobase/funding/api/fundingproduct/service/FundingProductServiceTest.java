package com.zerobase.funding.api.fundingproduct.service;

import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;
import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.funding.service.FundingService;
import com.zerobase.funding.api.fundingproduct.dto.DetailResponse;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.dto.model.RewardDto;
import com.zerobase.funding.api.fundingproduct.exception.FundingProductException;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.api.s3.AwsS3Service;
import com.zerobase.funding.api.s3.dto.S3FileDto;
import com.zerobase.funding.common.builder.MemberBuilder;
import com.zerobase.funding.common.constants.RewardConstants;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.image.entity.Image;
import com.zerobase.funding.domain.image.entity.ImageType;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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

    @Mock
    FundingService fundingService;

    @Mock
    ViewsService viewsService;

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

        given(viewsService.getViews(any(), any()))
                .willReturn(1);

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
                .willThrow(new AuthException(MEMBER_NOT_FOUND));

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
        assertEquals(MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("펀딩 상품 등록 실패 - 내부 에러 발생 시 S3 파일 삭제가 실행된다.")
    void registration_exception_occurred() throws IOException {
        // given
        given(authenticationService.getMemberOrThrow(any()))
                .willReturn(MemberBuilder.member());

        S3FileDto fileThumbnail = new S3FileDto("thumbnail_url", "");
        given(awsS3Service.uploadFile(any()))
                .willReturn(fileThumbnail);

        List<S3FileDto> fileDetails = List.of(new S3FileDto("detail_url", ""));
        given(awsS3Service.uploadFiles(any()))
                .willReturn(fileDetails);

        given(fundingProductRepository.save(any()))
                .willThrow(new FundingProductException(INTERNAL_ERROR));

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

        FundingProductException exception = assertThrows(
                FundingProductException.class, () ->
                        fundingProductService.registration(request, thumbnail, List.of(details),
                                MEMBER_KEY));

        ArgumentCaptor<S3FileDto> captor = ArgumentCaptor.forClass(S3FileDto.class);
        ArgumentCaptor<List<S3FileDto>> captor2 = ArgumentCaptor.forClass(List.class);

        // then
        verify(awsS3Service, times(1)).deleteFile(captor.capture());
        verify(awsS3Service, times(1)).deleteFiles(captor2.capture());
        assertEquals(INTERNAL_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("펀딩 상품 조회 성공")
    void detail() {
        // given
        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(getFundingProduct()));

        given(fundingService.findByRewards(any()))
                .willReturn(List.of(Funding.builder().fundingPrice(70000).build(),
                        Funding.builder().fundingPrice(5800).build()));

        given(viewsService.saveOrUpdate(any(), any()))
                .willReturn(1);

        MockedStatic<LocalDate> localTimeMock =
                mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);

        LocalDate localDate = LocalDate.of(2023, 12, 27);
        localTimeMock.when(LocalTime::now)
                .thenReturn(localDate);

        // when
        DetailResponse response = fundingProductService.detail(1L);

        // then
        assertEquals(12, response.getRemainingDays());
        assertEquals(75800, response.getTotalAmount());
        assertEquals(15, response.getCompletionPercent());
        assertEquals(2, response.getDonorCount());
    }

    @Test
    @DisplayName("펀딩 상품 조회 실패 - 없는 상품")
    void detail_funding_product_not_found() {
        // given
        given(fundingProductRepository.findByIdFetch(any()))
                .willThrow(new FundingProductException(FUNDING_PRODUCT_NOT_FOUND));

        // when
        FundingProductException exception = assertThrows(FundingProductException.class,
                () -> fundingProductService.detail(1L));

        // then
        assertEquals(FUNDING_PRODUCT_NOT_FOUND, exception.getErrorCode());
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