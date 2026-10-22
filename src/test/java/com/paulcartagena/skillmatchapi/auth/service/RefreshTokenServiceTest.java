package com.paulcartagena.skillmatchapi.auth.service;

import com.paulcartagena.skillmatchapi.auth.entity.RefreshToken;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.repository.RefreshTokenRepository;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 7L);

        user = new User();
        user.setEmail("user@user.com");
    }

    @Test
    void createRefreshToken_revokesPreviousTokens_beforeSavingNewOne() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        InOrder inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).revokeAllByUser(user);
        inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_buildsUnrevokedUuidTokenExpiringInConfiguredDays() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(user);

        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.isRevoked()).isFalse();
        assertThat(UUID.fromString(token.getToken())).isNotNull();
        assertThat(token.getExpiresAt())
                .isCloseTo(Instant.now().plus(7, ChronoUnit.DAYS), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void verifyAndGet_returnsToken_whenValid() {
        RefreshToken stored = refreshToken(false, Instant.now().plus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(stored));

        assertThat(refreshTokenService.verifyAndGet("valid-token")).isSameAs(stored);
    }

    @Test
    void verifyAndGet_throwsUnauthorized_whenTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyAndGet("missing-token"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void verifyAndGet_throwsUnauthorized_whenTokenIsRevoked() {
        RefreshToken stored = refreshToken(true, Instant.now().plus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> refreshTokenService.verifyAndGet("revoked-token"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void verifyAndGet_throwsUnauthorized_whenTokenIsExpired() {
        RefreshToken stored = refreshToken(false, Instant.now().minus(1, ChronoUnit.MINUTES));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> refreshTokenService.verifyAndGet("expired-token"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void revokeAllByUser_delegatesToRepository() {
        refreshTokenService.revokeAllByUser(user);

        verify(refreshTokenRepository).revokeAllByUser(user);
    }

    private RefreshToken refreshToken(boolean revoked, Instant expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setRevoked(revoked);
        refreshToken.setExpiresAt(expiresAt);
        return refreshToken;
    }
}
