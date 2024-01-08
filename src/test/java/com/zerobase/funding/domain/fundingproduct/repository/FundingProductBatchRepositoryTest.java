package com.zerobase.funding.domain.fundingproduct.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.redis.entity.Views;
import com.zerobase.funding.domain.redis.repository.ViewsRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class FundingProductBatchRepositoryTest {

    @Autowired
    FundingProductBatchRepository fundingProductBatchRepository;

    @Autowired
    FundingProductRepository fundingProductRepository;

    @Autowired
    ViewsRepository viewsRepository;

    @BeforeEach
    void setup() {
        for (int i = 1; i <= 3; i++) {
            FundingProduct fundingProduct = FundingProduct.builder()
                    .title("제품")
                    .description("설명")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .targetAmount(10000)
                    .build();

            fundingProduct.setViews(0);

            fundingProductRepository.save(fundingProduct);
            viewsRepository.save(new Views(String.valueOf(i), i));
        }
    }

    @Test
    @DisplayName("펀딩 상품 조회수 배치 업데이트")
    void findFundingProducts_in_progress() {
        // given
        List<Views> views = IntStream.range(1, 4)
                .mapToObj(o -> viewsRepository.findById(String.valueOf(o)).get())
                .toList();

        // when
        fundingProductBatchRepository.updateAll(views);

        // then
        List<FundingProduct> fundingProducts = LongStream.range(1, 4)
                .mapToObj(o -> fundingProductRepository.findById(o).get())
                .toList();

        assertEquals(1, fundingProducts.get(0).getViews());
        assertEquals(2, fundingProducts.get(1).getViews());
        assertEquals(3, fundingProducts.get(2).getViews());
    }
}
