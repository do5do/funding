package com.zerobase.funding.api.fundingproduct.scheduler;

import com.zerobase.funding.api.fundingproduct.service.ViewsService;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductBatchRepository;
import com.zerobase.funding.domain.redis.entity.Views;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ViewsScheduler {

    private final ViewsService viewsService;
    private final FundingProductBatchRepository fundingProductBatchRepository;

    @Scheduled(cron = "${scheduler.api.funding-product.views}")
    @Transactional
    public void updateViews() { // 하루에 한번 조회수 업데이트 (redis -> db)
        long start = System.currentTimeMillis();
        log.info("start scheduling -> update fundingProduct(views).");
        List<Views> views = viewsService.findAll();

        fundingProductBatchRepository.updateAll(views);
        log.info("update fundingProduct(views) finished {}ms", System.currentTimeMillis() - start);
    }
}
