package com.zerobase.funding.api.auth.service;

import com.zerobase.funding.domain.redis.entity.RefreshToken;
import com.zerobase.funding.domain.redis.repository.RefreshTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public String findByIdOrNull(String memberKey) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(memberKey);
        return refreshToken.map(RefreshToken::getRefreshToken).orElse(null);
    }

    public void deleteRefreshToken(String memberKey) {
        refreshTokenRepository.deleteById(memberKey);
    }

    @Transactional
    public void saveOrUpdate(String memberKey, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findById(memberKey)
                .map(o -> o.update(refreshToken))
                .orElseGet(() -> new RefreshToken(memberKey, refreshToken));

        refreshTokenRepository.save(token);
    }
}
