package com.zerobase.funding.api.fundingproduct.controller;

import com.zerobase.funding.api.auth.annotaion.RoleUser;
import com.zerobase.funding.api.fundingproduct.dto.DetailResponse;
import com.zerobase.funding.api.fundingproduct.dto.Edit;
import com.zerobase.funding.api.fundingproduct.dto.RegistrationRequest;
import com.zerobase.funding.api.fundingproduct.dto.SearchCondition;
import com.zerobase.funding.api.fundingproduct.dto.model.FundingProductDto;
import com.zerobase.funding.api.fundingproduct.service.FundingProductService;
import com.zerobase.funding.api.common.validatiion.ValidFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RequestMapping("/funding-products")
@RequiredArgsConstructor
@RestController
public class FundingProductController {

    private final FundingProductService fundingProductService;

    /**
     * 펀딩 상품 목록 조회
     *
     * @param pageable        페이지
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
     *
     * @param request     등록 요청 정보
     * @param thumbnail   상품 썸네일 이미지
     * @param details     상품 디테일 이미지
     * @param userDetails 인증 유저 정보
     * @return 201 created
     */
    @RoleUser
    @PostMapping
    public ResponseEntity<Void> registration(@RequestPart @Valid RegistrationRequest request,
            @RequestPart @ValidFile MultipartFile thumbnail,
            @RequestPart @Size(min = 1, max = 5) List<@ValidFile MultipartFile> details,
            @AuthenticationPrincipal UserDetails userDetails) {
        FundingProductDto fundingProductDto = fundingProductService.registration(request, thumbnail,
                details, userDetails.getUsername());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(fundingProductDto.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * 펀딩 상품 상세
     *
     * @param id 펀딩 상품 아이디
     * @return 펀딩 상품 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<DetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(fundingProductService.detail(id));
    }

    /**
     * 펀딩 상품 수정
     *
     * @param id 펀딩 상품 아이디
     * @param request 수정 요청 정보
     * @param userDetails 인증 유저
     * @return 수정된 상품 정보
     */
    @RoleUser
    @PatchMapping("/{id}")
    public ResponseEntity<Edit.Response> edit(@PathVariable Long id,
            @RequestBody @Valid Edit.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                fundingProductService.edit(id, request, userDetails.getUsername()));
    }

    /**
     * 펀딩 상품 삭제
     *
     * @param id 펀딩 상품 아이디
     * @param userDetails 인증 유저
     * @return 204 noContent
     */
    @RoleUser
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        fundingProductService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
