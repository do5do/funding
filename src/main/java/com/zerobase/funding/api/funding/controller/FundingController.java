package com.zerobase.funding.api.funding.controller;

import com.zerobase.funding.api.auth.annotaion.RoleUser;
import com.zerobase.funding.api.funding.dto.CreateFunding;
import com.zerobase.funding.api.funding.service.FundingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/funding")
@RequiredArgsConstructor
@RestController
public class FundingController {

    private final FundingService fundingService;

    /**
     * 펀딩하기
     * @param request 펀딩 요청 정보
     * @param userDetails 인증 유저
     * @return 펀딩 정보
     */
    @RoleUser
    @PostMapping
    public ResponseEntity<CreateFunding.Response> createFunding(
            @RequestBody @Valid CreateFunding.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                fundingService.createFunding(request, userDetails.getUsername()));
    }
}
