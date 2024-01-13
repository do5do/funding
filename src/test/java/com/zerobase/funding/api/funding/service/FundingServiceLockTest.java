package com.zerobase.funding.api.funding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zerobase.funding.api.funding.dto.CreateFunding.Request;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.member.entity.Address;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.entity.Role;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import com.zerobase.funding.domain.reward.entity.Reward;
import com.zerobase.funding.domain.reward.repository.RewardRepository;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FundingServiceLockTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RewardRepository rewardRepository;

    @Autowired
    FundingProductRepository fundingProductRepository;

    @Autowired
    FundingService fundingService;

    // 현재 hikariCP에서 허용가능한 범위로 설정 (pool size를 20으로 설정해둬서 20개부터는 테스트 실패,
    // thransaction이 한번 더 여리는 이유로 pool size에 따라 실패 여부가 갈림)
    int threadCount = 19;

    @BeforeEach
    void setup() {
        FundingProduct fundingProduct = fundingProductRepository.save(
                FundingProduct.builder()
                        .title("제품")
                        .description("설명")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now())
                        .targetAmount(10000)
                        .build());

        Reward reward = Reward.builder()
                .title("title")
                .description("desc")
                .price(10000)
                .stockQuantity(100)
                .build();

        reward.setFundingProduct(fundingProduct);
        rewardRepository.save(reward);

        for (int i = 0; i < threadCount; i++) {
            Member member = Member.builder()
                    .name("aa")
                    .email(i + "test@gmail.com")
                    .profile("bbb")
                    .role(Role.USER)
                    .memberKey(String.valueOf(i))
                    .build();

            member.addAddress(Address.builder()
                    .roadAddress("road")
                    .addressDetail("detail")
                    .zipcode("1234-56")
                    .build());

            memberRepository.save(member);
        }
    }

    @Test
    @DisplayName("리워드 수량 감소 동시성 제어")
    void createFunding_concurrency_control() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        Request request = new Request(1L, 1L, null);

        // when
        IntStream.range(0, threadCount).forEach(o -> executorService.execute(() -> {
            try {
                fundingService.createFunding(request, String.valueOf(o));
            } finally {
                countDownLatch.countDown();
            }
        }));

        countDownLatch.await();

        // then
        Reward reward = rewardRepository.findById(1L).get();
        assertEquals(100 - threadCount, reward.getStockQuantity());
    }
}
