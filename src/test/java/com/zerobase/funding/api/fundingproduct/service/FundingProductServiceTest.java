package com.zerobase.funding.api.fundingproduct.service;

import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_EDIT;
import static com.zerobase.funding.api.exception.ErrorCode.FUNDING_PRODUCT_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;
import static com.zerobase.funding.api.exception.ErrorCode.INVALID_DATE;
import static com.zerobase.funding.api.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zerobase.funding.api.exception.ErrorCode.NO_ACCESS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
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
import com.zerobase.funding.api.s3.dto.S3FileDto;
import com.zerobase.funding.api.s3.service.AwsS3Service;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.image.entity.Image;
import com.zerobase.funding.domain.image.entity.ImageType;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.reward.entity.Reward;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

    String memberKey = "key";
    String title = "title";
    String description = "desc";
    LocalDate startDate = LocalDate.of(2023, 12, 8);
    LocalDate endDate = LocalDate.of(2024, 1, 8);
    Integer targetAmount = 500000;
    Integer rewardPrice = 35000;
    Integer stockQuantity = 100;

    FundingProduct fundingProduct = FundingProduct.builder()
            .title(title)
            .description(description)
            .startDate(startDate)
            .endDate(endDate)
            .targetAmount(targetAmount)
            .build();

    Member member = Member.builder()
            .memberKey(memberKey)
            .build();

    LocalDate now = LocalDate.of(2023, 12, 27);

    private RegistrationRequest registrationRequest(LocalDate startDate, LocalDate endDate) {
        return RegistrationRequest.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .targetAmount(targetAmount)
                .rewards(List.of(RewardDto.builder()
                        .title(title)
                        .description(description)
                        .price(rewardPrice)
                        .stockQuantity(stockQuantity)
                        .build()))
                .build();
    }

    @Nested
    @DisplayName("펀딩 상품 목록 조회 메소드")
    class FundingProductsMethod {

        Reward reward = Reward.builder()
                .title(title)
                .description(description)
                .price(rewardPrice)
                .stockQuantity(stockQuantity)
                .build();

        @Test
        @DisplayName("펀딩 상품 목록 조회 성공")
        void fundingProducts() {
            // given
            fundingProduct.addMember(member);
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
    }

    @Nested
    @DisplayName("펀딩 상품 등록 메소드")
    class RegistrationMethod {

        MockMultipartFile thumbnail;
        MockMultipartFile details;

        @BeforeEach
        void setup() throws Exception {
            thumbnail = new MockMultipartFile("thumbnail", "thumbnail.webp",
                    "webp", new FileInputStream("src/test/resources/img/thumbnail.webp"));

            details = new MockMultipartFile("details", "java.png",
                    "png", new FileInputStream("src/test/resources/img/java.png"));
        }

        @Test
        @DisplayName("성공")
        void registration() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(awsS3Service.uploadFile(any()))
                    .willReturn(CompletableFuture.completedFuture(
                            new S3FileDto("thumbnail_url", "")));

            given(fundingProductRepository.save(any()))
                    .willReturn(fundingProduct);

            // when
            RegistrationRequest request = registrationRequest(startDate, endDate);

            fundingProductService.registration(request, thumbnail, List.of(details), memberKey);

            ArgumentCaptor<FundingProduct> captor = ArgumentCaptor.forClass(FundingProduct.class);

            // then
            verify(fundingProductRepository, times(1)).save(captor.capture());
            assertEquals(title, captor.getValue().getTitle());
        }

        @Test
        @DisplayName("실패 - 없는 회원")
        void registration_member_not_found() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willThrow(new AuthException(MEMBER_NOT_FOUND));

            // when
            RegistrationRequest request = registrationRequest(startDate, endDate);

            AuthException exception = assertThrows(AuthException.class, () ->
                    fundingProductService.registration(request, thumbnail, List.of(details),
                            memberKey));

            // then
            assertEquals(MEMBER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 시작 날짜는 완료 날짜 이전이어야 한다.")
        void registration_valid_date() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            // when
            RegistrationRequest request = registrationRequest(
                    LocalDate.of(2023, 12, 29),
                    LocalDate.of(2023, 12, 28));

            FundingProductException exception = assertThrows(FundingProductException.class,
                    () -> fundingProductService.registration(request, thumbnail, List.of(details),
                            memberKey));

            // then
            assertEquals(INVALID_DATE, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 내부 에러 발생 시 S3 파일 삭제가 실행된다.")
        void registration_exception_occurred() {
            // given
            given(authenticationService.getMemberOrThrow(any()))
                    .willReturn(member);

            given(awsS3Service.uploadFile(any()))
                    .willReturn(CompletableFuture
                            .completedFuture(new S3FileDto("thumbnail_url", "")));

            given(fundingProductRepository.save(any()))
                    .willThrow(new FundingProductException(INTERNAL_ERROR));

            // when
            RegistrationRequest request = registrationRequest(startDate, endDate);

            FundingProductException exception = assertThrows(FundingProductException.class,
                    () -> fundingProductService.registration(request, thumbnail, List.of(details),
                            memberKey));

            // then
            verify(awsS3Service, times(2)).deleteFile(anyString());
            assertEquals(INTERNAL_ERROR, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("펀딩 상품 조회 메소드")
    class DetailMethod {

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class, CALLS_REAL_METHODS);

        @AfterEach
        void close() {
            localDateMock.close();
        }

        @Test
        @DisplayName("성공")
        void detail() {
            // given
            given(fundingProductRepository.findByIdAndDeleted(any(), anyBoolean()))
                    .willReturn(Optional.of(fundingProduct));

            Funding funding = Funding.builder()
                    .fundingPrice(10000)
                    .build();

            given(fundingService.getFundingByRewards(any()))
                    .willReturn(List.of(funding, funding));

            given(viewsService.saveOrUpdate(any(), any()))
                    .willReturn(1);

            localDateMock.when(LocalDate::now).thenReturn(now);

            // when
            DetailResponse response = fundingProductService.detail(1L);

            // then
            assertEquals(12, response.getRemainingDays());
            assertEquals(20000, response.getTotalAmount());
            assertEquals(4, response.getCompletionPercent());
            assertEquals(2, response.getDonorCount());
        }

        @Test
        @DisplayName("실패 - 없는 상품")
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
    }

    @Nested
    @DisplayName("펀딩 상품 수정 메소드")
    class EditMethod {

        FundingProduct fundingProduct = FundingProduct.builder()
                .title(title)
                .startDate(LocalDate.of(2023, 12, 28))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class, CALLS_REAL_METHODS);

        String updateTitle = "제목 수정";
        Request request = Request.builder()
                .title(updateTitle)
                .build();

        @AfterEach
        void close() {
            localDateMock.close();
        }

        @Test
        @DisplayName("펀딩 상품 수정 성공")
        void edit() {
            // given
            given(fundingProductRepository.findByIdFetchMember(any()))
                    .willReturn(Optional.of(fundingProduct));

            doNothing().when(authenticationService).checkAccess(any(), any());

            localDateMock.when(LocalDate::now).thenReturn(now);

            // when
            Response response = fundingProductService.edit(1L, request, memberKey);

            // then
            assertEquals(updateTitle, response.title());
        }

        @Test
        @DisplayName("실패 - 없는 펀딩 상품")
        void edit_fundingProduct_not_found() {
            // given
            given(fundingProductRepository.findByIdFetchMember(any()))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> fundingProductService.edit(1L, request, memberKey))
                    .isInstanceOf(FundingProductException.class)
                    .hasMessageContaining(FUNDING_PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패 - 로그인한 유저와 상품을 등록한 유저가 다르면 안된다.")
        void edit_no_access() {
            // given
            given(fundingProductRepository.findByIdFetchMember(any()))
                    .willReturn(Optional.of(fundingProduct));

            doThrow(new AuthException(NO_ACCESS)).when(authenticationService)
                    .checkAccess(any(), any());

            // when
            // then
            assertThatThrownBy(() -> fundingProductService.edit(1L, request, memberKey))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining(NO_ACCESS.getMessage());
        }

        @Test
        @DisplayName("실패 - 펀딩 진행중인 상품은 수정할 수 없다. (시작 날짜 < 현재)")
        void edit_startDate_isBefore() {
            // given
            FundingProduct fundingProduct = FundingProduct.builder()
                    .startDate(LocalDate.of(2023, 12, 26))
                    .endDate(LocalDate.of(2023, 12, 28))
                    .build();

            given(fundingProductRepository.findByIdFetchMember(any()))
                    .willReturn(Optional.of(fundingProduct));

            doNothing().when(authenticationService).checkAccess(any(), any());

            localDateMock.when(LocalDate::now).thenReturn(now);

            // when
            // then
            assertThatThrownBy(() -> fundingProductService.edit(1L, request, memberKey))
                    .isInstanceOf(FundingProductException.class)
                    .hasMessageContaining(FUNDING_PRODUCT_NOT_EDIT.getMessage());
        }

        @Test
        @DisplayName("실패 - 펀딩 진행중인 상품은 수정할 수 없다. (시작 날짜 == 현재)")
        void edit_startDate_isEqual() {
            // given
            FundingProduct fundingProduct = FundingProduct.builder()
                    .startDate(LocalDate.of(2023, 12, 27))
                    .endDate(LocalDate.of(2023, 12, 28))
                    .build();

            given(fundingProductRepository.findByIdFetchMember(any()))
                    .willReturn(Optional.of(fundingProduct));

            doNothing().when(authenticationService).checkAccess(any(), any());

            localDateMock.when(LocalDate::now).thenReturn(now);

            // when
            // then
            assertThatThrownBy(() -> fundingProductService.edit(1L, request, memberKey))
                    .isInstanceOf(FundingProductException.class)
                    .hasMessageContaining(FUNDING_PRODUCT_NOT_EDIT.getMessage());
        }
    }

    @Nested
    @DisplayName("펀딩 상품 삭제 메소드")
    class DeleteMethod {
        FundingProduct fundingProduct = FundingProduct.builder()
                .startDate(LocalDate.of(2023, 12, 28))
                .endDate(LocalDate.of(2023, 12, 28))
                .build();

        MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class, CALLS_REAL_METHODS);

        @AfterEach
        void close() {
            localDateMock.close();
        }

        @Test
        @DisplayName("성공")
        void delete() {
            // given
            List.of(new Image(ImageType.THUMBNAIL, "", ""))
                    .forEach(fundingProduct::addImages);

            given(fundingProductRepository.findByIdFetchMember(any()))
                    .willReturn(Optional.of(fundingProduct));

            doNothing().when(authenticationService).checkAccess(any(), any());

            localDateMock.when(LocalDate::now).thenReturn(now);

            doNothing().when(viewsService).deleteViews(any());
            doNothing().when(awsS3Service).deleteFile(any());

            // when
            fundingProductService.delete(1L, memberKey);

            // then
            verify(viewsService, times(1)).deleteViews(anyString());
            verify(awsS3Service, times(1)).deleteFile(any());
            assertTrue(fundingProduct.isDeleted());
        }
    }
}