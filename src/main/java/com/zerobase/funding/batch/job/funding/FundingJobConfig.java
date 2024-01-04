package com.zerobase.funding.batch.job.funding;

import static com.zerobase.funding.domain.delivery.entity.Status.CANCEL;
import static com.zerobase.funding.domain.delivery.entity.Status.SHIPPING;
import static com.zerobase.funding.domain.funding.entity.Status.COMPLETE;
import static com.zerobase.funding.domain.funding.entity.Status.FAIL;
import static com.zerobase.funding.domain.funding.entity.Status.IN_PROGRESS;

import com.zerobase.funding.batch.constants.BeanPrefix;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.funding.repository.FundingRepository;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FundingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final FundingRepository fundingRepository;

    private static final Integer CHUNK_SIZE = 100;

    @Bean(name = BeanPrefix.FUNDING_ENDED + "Job")
    public Job job() {
        return new JobBuilder(BeanPrefix.FUNDING_ENDED + "Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }

    @Bean(name = BeanPrefix.FUNDING_ENDED + "Step")
    @JobScope
    public Step step() {
        return new StepBuilder(BeanPrefix.FUNDING_ENDED + "Step", jobRepository)
                .<FundingProduct, List<Funding>>chunk(CHUNK_SIZE, transactionManager)
                .reader(jpaCursorItemReader())
                .processor(itemProcessor())
                .writer(jpaItemWriter())
                .build();
    }

    @Bean(name = BeanPrefix.FUNDING_ENDED + "Reader")
    @StepScope
    public JpaCursorItemReader<FundingProduct> jpaCursorItemReader() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("now", LocalDate.now());

        return new JpaCursorItemReaderBuilder<FundingProduct>()
                .name(BeanPrefix.FUNDING_ENDED + "Reader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select fp from FundingProduct fp"
                        + " join fetch fp.rewards r"
                        + " where fp.endDate = :now")
                .parameterValues(parameters)
                .build();
    }

    @Bean(name = BeanPrefix.FUNDING_ENDED + "Processor")
    public ItemProcessor<FundingProduct, List<Funding>> itemProcessor() {
        return fundingProduct -> {
            List<Funding> fundingList = fundingRepository.findAllByRewardInAndStatusFetch(
                    fundingProduct.getRewards(), IN_PROGRESS);

            int totalAmount = fundingList.size() * fundingList.get(0).getFundingPrice();

            if (totalAmount >= fundingProduct.getTargetAmount()) { // 펀딩 성공
                fundingList.forEach(funding -> {
                    funding.updateStatus(COMPLETE);
                    funding.getDelivery().updateStatus(SHIPPING);
                    // todo 알림 발송
                });
            } else { // 펀딩 실패
                fundingList.forEach(funding -> {
                    funding.updateStatus(FAIL);
                    funding.getDelivery().updateStatus(CANCEL);
                    // todo 알림 발송
                });
            }

            return fundingList;
        };
    }

    @Bean(name = BeanPrefix.FUNDING_ENDED + "Writer")
    public JpaItemWriter<List<Funding>> jpaItemWriter() {
        JpaItemWriter<List<Funding>> writer = new JpaItemWriter<>() {
            @Override
            public void write(Chunk<? extends List<Funding>> chunk) {
                Chunk<Funding> total = new Chunk<>();

                for (List<Funding> fundings : chunk.getItems()) {
                    for (Funding funding : fundings) {
                        total.add(funding);
                    }
                }

                JpaItemWriter<Funding> delegator = new JpaItemWriter<>();
                delegator.setEntityManagerFactory(entityManagerFactory);
                delegator.write(total);
            }
        };

        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
