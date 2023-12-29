package com.zerobase.funding.domain.fundingproduct.repository;

import com.zerobase.funding.domain.redis.entity.Views;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class FundingProductBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void updateAll(List<Views> views) {
        List<String> ids = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        sb.append("update funding_product set modified_date = now(), views = case id");

        views.forEach(o -> {
            sb.append(" when ").append(o.getId())
                    .append(" then ").append(o.getCount());
            ids.add(o.getId());
        });

        sb.append(" else views end where id in (%s)");

        String sql = String.format(sb.toString(), String.join(",", ids));
        jdbcTemplate.update(sql);
    }
}
