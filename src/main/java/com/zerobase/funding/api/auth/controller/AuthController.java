package com.zerobase.funding.api.auth.controller;

import com.zerobase.funding.api.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController {
    @GetMapping("/auth/success")
    public ResponseEntity<LoginResponse> loginSuccess(LoginResponse loginResponse) {
        return ResponseEntity.ok(loginResponse);
    }
}
