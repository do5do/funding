package com.zerobase.funding.api.fundingproduct.controller;

import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.service.FundingProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/funding-products")
@RequiredArgsConstructor
@RestController
public class FundingProductController {

    private final FundingProductService fundingProductService;

    @GetMapping
    public ResponseEntity<Slice<FundingProductDto>> fundingProducts(
            @PageableDefault(sort = "id", direction = Direction.DESC) final Pageable pageable,
            @Valid SearchCondition searchCondition) {
        return ResponseEntity.ok(fundingProductService.fundingProducts(pageable, searchCondition));
    }
}
