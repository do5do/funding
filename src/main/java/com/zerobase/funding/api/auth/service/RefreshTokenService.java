package com.zerobase.funding.api.auth.service;

import static com.zerobase.funding.api.exception.ErrorCode.TOKEN_NOT_FOUND;

import com.zerobase.funding.api.auth.exception.AuthException;
import com.zerobase.funding.domain.redis.entity.RefreshToken;
import com.zerobase.funding.domain.redis.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String memberKey, String accessToken, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(memberKey, accessToken, refreshToken));
    }

    @Transactional(readOnly = true)
    public String findByAccessTokenOrThrow(String accessToken) {
        return refreshTokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new AuthException(TOKEN_NOT_FOUND)).getRefreshToken();
    }
}
