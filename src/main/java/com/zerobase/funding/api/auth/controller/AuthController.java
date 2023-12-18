package com.zerobase.funding.api.auth.controller;

import com.zerobase.funding.api.auth.dto.LoginResponse;
import com.zerobase.funding.api.auth.dto.LogoutResponse;
import com.zerobase.funding.api.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    @GetMapping("/auth/success")
    public ResponseEntity<LoginResponse> loginSuccess(@Valid LoginResponse loginResponse) {
        return ResponseEntity.ok(loginResponse);
    }

    @DeleteMapping("/auth/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new LogoutResponse(
                refreshTokenService.deleteRefreshToken(userDetails.getUsername())));
    }
}
