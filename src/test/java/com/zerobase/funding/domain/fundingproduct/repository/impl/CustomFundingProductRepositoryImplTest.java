package com.zerobase.funding.domain.fundingproduct.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import com.zerobase.funding.api.fundingproduct.type.SortType;
import com.zerobase.funding.config.TestQuerydslConfig;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.entity.Role;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestQuerydslConfig.class)
class CustomFundingProductRepositoryImplTest {

    @Autowired
    FundingProductRepository fundingProductRepository;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        Member member = memberRepository.save(Member.builder()
                .name("aa")
                .email("test@gmail.com")
                .profile("bbb")
                .role(Role.USER)
                .memberKey("key")
                .build());

        for (int i = 0; i < 3; i++) {
            FundingProduct fundingProduct = FundingProduct.builder()
                    .title("제품 " + i)
                    .description("설명 " + i)
                    .startDate(LocalDate.now().plusDays(i - 1))
                    .endDate(LocalDate.now().plusDays(30))
                    .targetAmount(10000)
                    .build();
            fundingProduct.addMember(member);
            fundingProduct.setViews(i);

            fundingProductRepository.save(fundingProduct);
        }
    }

    @Test
    @DisplayName("펀딩 상품 목록 조회 - 진행중인 상품, 최신순 정렬 검증")
    void findFundingProducts_in_progress() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);
        SearchCondition searchCondition =
                new SearchCondition(FilterType.IN_PROGRESS, null);

        // when
        Slice<FundingProduct> fundingProducts = fundingProductRepository.findFundingProducts(
                pageRequest, searchCondition);

        // then
        FundingProduct fundingProductFirst = fundingProducts.getContent().get(0);
        FundingProduct fundingProductSecond = fundingProducts.getContent().get(1);

        assertEquals(2, fundingProducts.getContent().size());
        assertTrue(fundingProductFirst.getStartDate().isBefore(LocalDate.now())
                || fundingProductFirst.getStartDate().isEqual(LocalDate.now()));
        assertTrue(fundingProductFirst.getId() > fundingProductSecond.getId());
    }

    @Test
    @DisplayName("펀딩 상품 목록 조회 - 진행중인 상품, 조회순 정렬 검증")
    void findFundingProducts_in_progress_sort_views() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);
        SearchCondition searchCondition =
                new SearchCondition(FilterType.IN_PROGRESS, SortType.VIEWS);

        // when
        Slice<FundingProduct> fundingProducts = fundingProductRepository.findFundingProducts(
                pageRequest, searchCondition);

        // then
        FundingProduct fundingProductFirst = fundingProducts.getContent().get(0);
        FundingProduct fundingProductSecond = fundingProducts.getContent().get(1);

        assertEquals(2, fundingProducts.getContent().size());
        assertTrue(fundingProductFirst.getStartDate().isBefore(LocalDate.now())
                || fundingProductFirst.getStartDate().isEqual(LocalDate.now()));
        assertTrue(fundingProductFirst.getViews() > fundingProductSecond.getViews());
    }

    @Test
    @DisplayName("펀딩 상품 목록 조회 - 오픈 예정 검증")
    void findFundingProducts_upcoming() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);
        SearchCondition searchCondition =
                new SearchCondition(FilterType.UPCOMING, null);

        // when
        Slice<FundingProduct> fundingProducts = fundingProductRepository.findFundingProducts(
                pageRequest, searchCondition);

        // then
        assertEquals(1, fundingProducts.getContent().size());
        assertTrue(fundingProducts.getContent().get(0).getStartDate().isAfter(LocalDate.now()));
    }
}