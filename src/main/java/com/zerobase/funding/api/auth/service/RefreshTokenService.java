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

    public void saveRefreshToken(String memberKey, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(memberKey, refreshToken));
    }

    @Transactional(readOnly = true)
    public String findByIdOrNull(String memberKey) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(memberKey);
        return refreshToken.map(RefreshToken::getRefreshToken).orElse(null);
    }

    public String deleteRefreshToken(String memberKey) {
        refreshTokenRepository.deleteById(memberKey);
        return memberKey;
    }
}
