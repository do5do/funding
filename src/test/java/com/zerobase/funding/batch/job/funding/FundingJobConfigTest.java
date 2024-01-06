package com.zerobase.funding.batch.job.funding;


import static com.zerobase.funding.domain.delivery.entity.Status.CANCEL;
import static com.zerobase.funding.domain.delivery.entity.Status.SHIPPING;
import static com.zerobase.funding.domain.delivery.entity.Status.WAITING;
import static com.zerobase.funding.domain.funding.entity.Status.COMPLETE;
import static com.zerobase.funding.domain.funding.entity.Status.FAIL;
import static com.zerobase.funding.domain.funding.entity.Status.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.funding.domain.delivery.entity.Delivery;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.repository.FundingRepository;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.member.entity.Member;
import com.zerobase.funding.domain.member.entity.Role;
import com.zerobase.funding.domain.member.repository.MemberRepository;
import com.zerobase.funding.domain.reward.entity.Reward;
import com.zerobase.funding.domain.reward.repository.RewardRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class FundingJobConfigTest {

    @Autowired
    JobLauncherTestUtils testUtils;

    @Autowired
    FundingRepository fundingRepository;

    @Autowired
    RewardRepository rewardRepository;

    @Autowired
    FundingProductRepository fundingProductRepository;

    @Autowired
    MemberRepository memberRepository;

    List<Reward> rewards = new ArrayList<>();

    @BeforeEach
    void setup() {
        saveFundingProducts();
        saveFunding();
    }

    @Test
    @DisplayName("펀딩 종료 시점 배치 테스트 - 펀딩 성공/실패에 따라 상태가 변경된다.")
    void fundingEndedJob() throws Exception {
        // given
        JobParameters jobParameter = testUtils.getUniqueJobParameters();

        // when
        JobExecution jobExecution = testUtils.launchJob(jobParameter);

        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        FundingProduct successFundingProduct = fundingProductRepository.findByIdFetchReward(1L).get();
        FundingProduct failFundingProduct = fundingProductRepository.findByIdFetchReward(2L).get();

        List<Funding> completeFundings = fundingRepository.findAllByRewardInFetch(
                successFundingProduct.getRewards());

        List<Funding> failFundings = fundingRepository.findAllByRewardInFetch(
                failFundingProduct.getRewards());

        // 상태 검증
        assertTrue(completeFundings.stream().allMatch(o -> o.getStatus() == COMPLETE));
        assertTrue(failFundings.stream().allMatch(o -> o.getStatus() == FAIL));

        assertFalse(completeFundings.stream().anyMatch(o -> o.getDelivery().getStatus() != SHIPPING));
        assertFalse(failFundings.stream().anyMatch(o -> o.getDelivery().getStatus() != CANCEL));

        assertEquals(6, completeFundings.size());
        assertEquals(4, failFundings.size());
    }

    private void saveFundingProducts() {
        for (int i = 0; i < 2; i++) {
            FundingProduct fundingProduct =FundingProduct.builder()
                    .title(i + "제품")
                    .description("설명")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .targetAmount(60000)
                    .build();

            for (int j = 0; j < 3; j++) {
                Reward reward = Reward.builder()
                        .title(i + "title")
                        .description("desc")
                        .price(10000)
                        .stockQuantity(100)
                        .build();

                fundingProduct.addRewards(reward);
                rewards.add(reward);
            }

            fundingProductRepository.save(fundingProduct);
        }
    }

    private void saveFunding() {
        List<Funding> fundings = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Member member = Member.builder()
                    .name("aa")
                    .email(i + "test@gmail.com")
                    .profile("bbb")
                    .role(Role.USER)
                    .memberKey(i + "key")
                    .build();
            memberRepository.save(member);

            Funding funding = Funding.builder()
                    .fundingPrice(10000)
                    .status(IN_PROGRESS)
                    .build();

            funding.addMember(member);
            funding.addDelivery(Delivery.of(WAITING));
            fundings.add(funding);
        }

        // 총 리워드는 6개, 1234(펀딩 2회) 56(펀딩 1회)
        int idx = 0;
        for (Funding funding : fundings) {
            funding.addReward(rewards.get(idx));
            idx++;

            fundingRepository.save(funding);

            if (idx > rewards.size() - 1) {
                idx = 0;
            }
        }
    }
}