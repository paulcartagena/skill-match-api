package com.paulcartagena.skillmatchapi.auth.service;

import com.paulcartagena.skillmatchapi.auth.entity.RefreshToken;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private Long refreshTokenExpirationDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked. Login again.");
        }

        return refreshToken;
    }
}
