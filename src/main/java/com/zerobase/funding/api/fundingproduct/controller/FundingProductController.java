package com.zerobase.funding.api.fundingproduct.controller;

import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.service.FundingProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequestMapping("/funding-products")
@RequiredArgsConstructor
@RestController
public class FundingProductController {

    private final FundingProductService fundingProductService;

    /**
     * 펀딩 상품 목록 조회
     * @param pageable 페이지
     * @param searchCondition 필터, 정렬 조건
     * @return 펀딩 상품 목록
     */
    @GetMapping
    public ResponseEntity<Slice<FundingProductDto>> fundingProducts(
            @PageableDefault(sort = "id", direction = Direction.DESC) final Pageable pageable,
            @Valid SearchCondition searchCondition) {
        return ResponseEntity.ok(fundingProductService.fundingProducts(pageable, searchCondition));
    }

    /**
     * 펀딩 상품 등록
     * @param request 등록 요청 정보
     * @param thumbnail 상품 썸네일 이미지
     * @param details 상품 디테일 이미지
     * @param userDetails 인증 유저 정보
     * @return 201 created
     */
    @PostMapping
    public ResponseEntity<Void> registration(@RequestPart @Valid RegistrationRequest request,
            @RequestPart @NotNull MultipartFile thumbnail,
            @RequestPart @NotNull List<MultipartFile> details,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long savedFundingProductId = fundingProductService.registration(request, thumbnail,
                details, userDetails.getUsername());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedFundingProductId)
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
