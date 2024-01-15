package com.zerobase.funding.api.auth.controller;

import com.zerobase.funding.api.auth.dto.LoginResponse;
import com.zerobase.funding.api.auth.service.TokenService;
import com.zerobase.funding.notification.service.RedisMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final TokenService tokenService;
    private final RedisMessageService redisMessageService;

    @GetMapping("/auth/success")
    public ResponseEntity<LoginResponse> loginSuccess(@Valid LoginResponse loginResponse) {
        return ResponseEntity.ok(loginResponse);
    }

    @DeleteMapping("/auth/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        tokenService.deleteRefreshToken(userDetails.getUsername());
        redisMessageService.removeSubscribe(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
