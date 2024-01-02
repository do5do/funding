package com.zerobase.funding.api.fundingproduct.service;

import com.zerobase.funding.domain.redis.entity.Views;
import com.zerobase.funding.domain.redis.repository.ViewsRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ViewsService {

    private final ViewsRepository viewsRepository;

    @Transactional
    public Integer saveOrUpdate(String fundingProductId, Integer dbCount) {
        Views views = viewsRepository.findById(fundingProductId)
                .map(Views::increaseCount)
                .orElseGet(() -> new Views(fundingProductId, dbCount + 1));

        viewsRepository.save(views);
        return views.getCount();
    }

    public Integer getViews(String fundingProductId, Integer dbCount) {
        return viewsRepository.findById(fundingProductId)
                .map(Views::getCount)
                .orElse(dbCount);
    }

    public List<Views> findAll() {
        return StreamSupport
                .stream(viewsRepository.findAll().spliterator(), false)
                .toList();
    }

    @Transactional
    public void deleteViews(String fundingProductId) {
        viewsRepository.deleteById(fundingProductId);
    }
}
