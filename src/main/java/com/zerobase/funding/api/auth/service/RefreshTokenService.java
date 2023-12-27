package com.zerobase.funding.api.auth.service;

import com.zerobase.funding.domain.redis.entity.RefreshToken;
import com.zerobase.funding.domain.redis.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void deleteRefreshToken(String memberKey) {
        refreshTokenRepository.deleteById(memberKey);
    }

    @Transactional
    public void saveOrUpdate(String memberKey, String refreshToken, String accessToken) {
        RefreshToken token = refreshTokenRepository.findByAccessToken(accessToken)
                .map(o -> o.updateRefreshToken(refreshToken))
                .orElseGet(() -> new RefreshToken(memberKey, refreshToken, accessToken));

        refreshTokenRepository.save(token);
    }

    @Transactional
    public String updateOrNull(String accessToken) {
        return refreshTokenRepository.findByAccessToken(accessToken)
                .map(o -> o.updateAccessToken(accessToken).getRefreshToken())
                .orElse(null);
    }
}
