package com.zerobase.funding.api.fundingproduct.service;

import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_EDIT;
import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;
import static com.zerobase.funding.api.exception.ErrorCode.INVALID_DATE;
import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.NO_ACCESS;
import static com.zerobase.funding.common.constants.FundingProductConstants.DESCRIPTION;
import static com.zerobase.funding.common.constants.FundingProductConstants.END_DATE;
import static com.zerobase.funding.common.constants.FundingProductConstants.START_DATE;
import static com.zerobase.funding.common.constants.FundingProductConstants.TARGET_AMOUNT;
import static com.zerobase.funding.common.constants.FundingProductConstants.TITLE;
import static com.zerobase.funding.common.constants.MemberConstants.MEMBER_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.api.auth.service.AuthenticationService;
import com.zerobase.funding.api.funding.service.FundingService;
import com.zerobase.funding.api.fundingproduct.dto.DetailResponse;
import com.zerobase.funding.api.fundingproduct.dto.Edit.Request;
import com.zerobase.funding.api.fundingproduct.dto.Edit.Response;
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

    LocalDate NOW = LocalDate.of(2023, 12, 27);

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
        fundingProduct.addImages(new Image(ImageType.THUMBNAIL, "url", "name"));

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
        RegistrationRequest request = getRegistrationRequest(START_DATE, END_DATE);

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
        RegistrationRequest request = getRegistrationRequest(START_DATE, END_DATE);

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
    @DisplayName("펀딩 상품 등록 실패 - 시작 날짜는 완료 날짜 이전이어야 한다.")
    void registration_valid_date() throws IOException {
        // given
        given(authenticationService.getMemberOrThrow(any()))
                .willReturn(MemberBuilder.member());

        // when
        RegistrationRequest request = getRegistrationRequest(
                LocalDate.of(2023, 12, 29),
                LocalDate.of(2023, 12, 28));

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

        MockMultipartFile details = new MockMultipartFile("details", "java.png",
                "png", new FileInputStream("src/test/resources/img/java.png"));

        FundingProductException exception = assertThrows(FundingProductException.class, () ->
                fundingProductService.registration(request, thumbnail, List.of(details),
                        MEMBER_KEY));

        // then
        assertEquals(INVALID_DATE, exception.getErrorCode());
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
        RegistrationRequest request = getRegistrationRequest(
                START_DATE, END_DATE);

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

        MockMultipartFile details = new MockMultipartFile("details", "java.png",
                "png", new FileInputStream("src/test/resources/img/java.png"));

        FundingProductException exception = assertThrows(
                FundingProductException.class, () ->
                        fundingProductService.registration(request, thumbnail, List.of(details),
                                MEMBER_KEY));

        // then
        verify(awsS3Service, times(1)).deleteFile(anyString());
        verify(awsS3Service, times(1)).deleteFiles(anyList());
        assertEquals(INTERNAL_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("펀딩 상품 조회 성공")
    void detail() {
        // given
        FundingProduct fundingProduct = getFundingProduct();

        given(fundingProductRepository.findByIdAndDeleted(any(), anyBoolean()))
                .willReturn(Optional.of(fundingProduct));

        given(fundingService.findByRewards(any()))
                .willReturn(List.of(Funding.builder().fundingPrice(70000).build(),
                        Funding.builder().fundingPrice(5800).build()));

        given(viewsService.saveOrUpdate(any(), any()))
                .willReturn(1);

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        // when
        DetailResponse response = fundingProductService.detail(1L);

        // then
        assertEquals(12, response.getRemainingDays());
        assertEquals(75800, response.getTotalAmount());
        assertEquals(15, response.getCompletionPercent());
        assertEquals(2, response.getDonorCount());

        localDateMock.close();
    }

    @Test
    @DisplayName("펀딩 상품 조회 실패 - 없는 상품")
    void detail_funding_product_not_found() {
        // given
        given(fundingProductRepository.findByIdAndDeleted(any(), anyBoolean()))
                .willThrow(new FundingProductException(FUNDING_PRODUCT_NOT_FOUND));

        // when
        FundingProductException exception = assertThrows(FundingProductException.class,
                () -> fundingProductService.detail(1L));

        // then
        assertEquals(FUNDING_PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("펀딩 상품 수정 성공")
    void edit() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 28))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doNothing().when(authenticationService).checkAccess(any(), any());

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        // when
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        Response response = fundingProductService.edit(1L, request, MEMBER_KEY);

        // then
        assertEquals(title, response.title());
        localDateMock.close();
    }

    @Test
    @DisplayName("펀딩 상품 수정 실패 - 없는 펀딩 상품")
    void edit_fundingProduct_not_found() {
        // given
        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.empty());

        // when
        // then
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        assertThatThrownBy(() -> fundingProductService.edit(1L, request, MEMBER_KEY))
                .isInstanceOf(FundingProductException.class)
                .hasMessageContaining(FUNDING_PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("펀딩 상품 수정 실패 - 로그인한 유저와 상품을 등록한 유저가 다르면 안된다.")
    void edit_no_access() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 28))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doThrow(new AuthException(NO_ACCESS)).when(authenticationService)
                .checkAccess(any(), any());

        // when
        // then
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        assertThatThrownBy(() -> fundingProductService.edit(1L, request, MEMBER_KEY))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(NO_ACCESS.getMessage());
    }

    @Test
    @DisplayName("펀딩 상품 수정 실패 - 펀딩 진행중인 상품은 수정할 수 없다. (시작 날짜 < 현재)")
    void edit_startDate_isBefore() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 26))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doNothing().when(authenticationService).checkAccess(any(), any());

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        // when
        // then
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        assertThatThrownBy(() -> fundingProductService.edit(1L, request, MEMBER_KEY))
                .isInstanceOf(FundingProductException.class)
                .hasMessageContaining(FUNDING_PRODUCT_NOT_EDIT.getMessage());

        localDateMock.close();
    }

    @Test
    @DisplayName("펀딩 상품 수정 실패 - 펀딩 진행중인 상품은 수정할 수 없다. (시작 날짜 == 현재)")
    void edit_startDate_isEqual() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 27))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doNothing().when(authenticationService).checkAccess(any(), any());

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        // when
        // then
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        assertThatThrownBy(() -> fundingProductService.edit(1L, request, MEMBER_KEY))
                .isInstanceOf(FundingProductException.class)
                .hasMessageContaining(FUNDING_PRODUCT_NOT_EDIT.getMessage());

        localDateMock.close();
    }

    @Test
    @DisplayName("펀딩 상품 수정 실패 - 펀딩 종료된 상품은 수정할 수 없다. (완료 날짜 < 현재)")
    void edit_endDate_isBefore() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 29))
                .endDate(LocalDate.of(2023, 12, 26))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doNothing().when(authenticationService).checkAccess(any(), any());

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        // when
        // then
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        assertThatThrownBy(() -> fundingProductService.edit(1L, request, MEMBER_KEY))
                .isInstanceOf(FundingProductException.class)
                .hasMessageContaining(FUNDING_PRODUCT_NOT_EDIT.getMessage());

        localDateMock.close();
    }

    @Test
    @DisplayName("펀딩 상품 수정 실패 - 펀딩 진행중인 상품은 수정할 수 없다. (완료 날짜 == 현재)")
    void edit_endDate_isEqual() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 27))
                .endDate(LocalDate.of(2023, 12, 27))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doNothing().when(authenticationService).checkAccess(any(), any());

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        // when
        // then
        String title = "제목 수정";
        Request request = Request.builder()
                .title(title)
                .build();

        assertThatThrownBy(() -> fundingProductService.edit(1L, request, MEMBER_KEY))
                .isInstanceOf(FundingProductException.class)
                .hasMessageContaining(FUNDING_PRODUCT_NOT_EDIT.getMessage());

        localDateMock.close();
    }

    @Test
    @DisplayName("펀딩 상품 삭제 성공")
    void delete() {
        // given
        FundingProduct fundingProduct = FundingProduct.builder()
                .title(TITLE)
                .startDate(LocalDate.of(2023, 12, 28))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        given(fundingProductRepository.findByIdFetch(any()))
                .willReturn(Optional.of(fundingProduct));

        doNothing().when(authenticationService).checkAccess(any(), any());

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class,
                Mockito.CALLS_REAL_METHODS);
        localDateMock.when(LocalDate::now).thenReturn(NOW);

        doNothing().when(viewsService).deleteViews(any());
        doNothing().when(awsS3Service).deleteFiles(any());

        // when
        fundingProductService.delete(1L, MEMBER_KEY);

        // then
        verify(viewsService, times(1)).deleteViews(anyString());
        verify(awsS3Service, times(1)).deleteFiles(anyList());
        assertTrue(fundingProduct.isDeleted());
        localDateMock.close();
    }

    private static RegistrationRequest getRegistrationRequest(LocalDate startDate,
            LocalDate endDate) {
        return RegistrationRequest.builder()
                .title(TITLE)
                .description(DESCRIPTION)
                .startDate(startDate)
                .endDate(endDate)
                .targetAmount(TARGET_AMOUNT)
                .rewards(List.of(RewardDto.builder()
                        .title(RewardConstants.TITLE)
                        .description(RewardConstants.DESCRIPTION)
                        .price(RewardConstants.PRICE)
                        .stockQuantity(RewardConstants.STOCK_QUANTITY)
                        .build()))
                .build();
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