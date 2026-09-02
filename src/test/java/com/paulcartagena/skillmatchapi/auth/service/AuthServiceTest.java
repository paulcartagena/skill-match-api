package com.paulcartagena.skillmatchapi.auth.service;

import com.paulcartagena.skillmatchapi.auth.dto.AuthResponse;
import com.paulcartagena.skillmatchapi.auth.dto.LoginRequest;
import com.paulcartagena.skillmatchapi.auth.dto.RegisterRequest;
import com.paulcartagena.skillmatchapi.auth.entity.RefreshToken;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.AccountStatus;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUserAsCandidate() {
        RegisterRequest request = new RegisterRequest("new@user.com", "New User", "password1");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(refreshTokenWithValue("refresh-token"));

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("new@user.com")
                        && user.getPassword().equals("encoded-password")
                        && user.getRole() == UserRole.CANDIDATE));
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@user.com", "Name", "password1");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRecruiter_savesUserAsPendingRecruiter() {
        RegisterRequest request = new RegisterRequest("recruiter@company.com", "Recruiter", "password1");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");

        authService.registerRecruiter(request);

        verify(userRepository).save(argThat(user ->
                user.getRole() == UserRole.RECRUITER
                        && user.getStatus() == AccountStatus.PENDING_VERIFICATION));
    }

    @Test
    void registerRecruiter_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@company.com", "Name", "password1");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registerRecruiter(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokens_forValidCredentials() {
        LoginRequest request = new LoginRequest("user@user.com", "password1");
        User user = new User();
        user.setEmail(request.getEmail());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshTokenWithValue("refresh-token"));

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_throwsUnauthorized_whenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("user@user.com", "wrong-password");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_throwsForbidden_whenAccountIsPendingOrSuspended() {
        LoginRequest request = new LoginRequest("user@user.com", "password1");
        doThrow(new DisabledException("Account disabled"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void refresh_returnsNewTokens_forValidRefreshToken() {
        User user = new User();
        user.setEmail("user@user.com");
        RefreshToken storedToken = refreshTokenWithValue("old-refresh-token");
        storedToken.setUser(user);

        when(refreshTokenService.verifyAndGet("old-refresh-token")).thenReturn(storedToken);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshTokenWithValue("new-refresh-token"));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponse response = authService.refresh("old-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void logout_revokesAllRefreshTokens_forUserFoundByEmail() {
        User user = new User();
        user.setEmail("user@user.com");
        when(userRepository.findByEmail("user@user.com")).thenReturn(Optional.of(user));

        authService.logout("user@user.com");

        verify(refreshTokenService).revokeAllByUser(user);
    }

    @Test
    void logout_throwsUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout("missing@user.com"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private RefreshToken refreshTokenWithValue(String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        return refreshToken;
    }
}
