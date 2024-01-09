package com.zerobase.funding.api.fundingproduct.dto;

import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.domain.funding.entity.Funding;
import com.zerobase.funding.domain.fundingproduct.entity.FundingProduct;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailResponse {

    private FundingProductDto fundingProduct;
    private Integer remainingDays; // 남은 일수
    private Integer totalAmount; // 모인 금액
    private Integer completionPercent; // 달성 퍼센트
    private Integer donorCount; // 후원자 수

    public static DetailResponse fromEntity(FundingProduct fundingProduct,
            List<Funding> fundingList, Integer views) {
        DetailResponse response = DetailResponse.builder()
                .fundingProduct(FundingProductDto.fromEntity(fundingProduct, views))
                .build();

        response.setProperties(fundingList);
        return response;
    }

    public void setProperties(List<Funding> fundingList) {
        remainingDays = Period.between(LocalDate.now(), fundingProduct.endDate()).getDays();
        donorCount = fundingList.size();
        totalAmount = donorCount > 0 ? donorCount * fundingList.get(0).getFundingPrice() : 0;
        completionPercent = Math.toIntExact(
                Math.round((float) totalAmount / fundingProduct.targetAmount() * 100));
    }
}
