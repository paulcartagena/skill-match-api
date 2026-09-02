package com.paulcartagena.skillmatchapi.security;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(Keys.hmacShaKeyFor(
                    "test-secret-key-that-is-long-enough-for-hs256".getBytes()).getEncoded());

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMinutes", 15L);

        user = new User();
        user.setEmail("user@user.com");
        user.setRole(UserRole.CANDIDATE);
    }

    @Test
    void generateAccessToken_producesTokenWithMatchingUsername() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.getUsername(token)).isEqualTo("user@user.com");
    }

    @Test
    void isTokenValid_returnsTrue_forMatchingUserAndUnexpiredToken() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, (UserDetails) user)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateAccessToken(user);

        User otherUser = new User();
        otherUser.setEmail("other@user.com");
        otherUser.setRole(UserRole.CANDIDATE);

        assertThat(jwtService.isTokenValid(token, (UserDetails) otherUser)).isFalse();
    }

    @Test
    void isTokenValid_throwsExpiredJwtException_whenTokenIsExpired() {
        // JwtService doesn't catch parsing errors itself - callers (JwtFilter) are expected
        // to treat any exception from isTokenValid as "not authenticated".
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMinutes", -1L);
        String expiredToken = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, (UserDetails) user))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
