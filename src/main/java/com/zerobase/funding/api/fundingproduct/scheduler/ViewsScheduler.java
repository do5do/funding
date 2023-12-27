package com.zerobase.funding.api.fundingproduct.scheduler;

import com.zerobase.funding.api.fundingproduct.service.ViewsService;
import com.zerobase.funding.domain.fundingproduct.repository.FundingProductRepository;
import com.zerobase.funding.domain.redis.entity.Views;
import java.util.Map;
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
    private final FundingProductRepository fundingProductRepository;

    @Scheduled(cron = "${scheduler.funding-product.views}")
    @Transactional
    public void updateViews() { // 하루에 한번 조회수 업데이트 (redis -> db)
        long start = System.currentTimeMillis();
        log.info("start scheduling -> update fundingProduct(views).");
        Map<Long, Views> views = viewsService.findAll();

        fundingProductRepository.findByIdIn(views.keySet())
                .forEach(o -> o.setViews(views.get(o.getId()).getCount()));

        log.info("update fundingProduct(views) finished {}ms", System.currentTimeMillis() - start);
    }
}
