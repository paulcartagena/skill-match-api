package com.paulcartagena.skillmatchapi.auth.controller;

import com.paulcartagena.skillmatchapi.auth.dto.AuthResponse;
import com.paulcartagena.skillmatchapi.auth.dto.LoginRequest;
import com.paulcartagena.skillmatchapi.auth.dto.RecruiterRegisterRequest;
import com.paulcartagena.skillmatchapi.auth.dto.RegisterRequest;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.service.AuthService;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTestSupport {

    @MockitoBean
    private AuthService authService;

    @Test
    void register_returnsTokens_withoutAuthentication() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(new AuthResponse("access", "refresh"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@user.com\",\"name\":\"New User\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void register_returns400_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"name\":\"New User\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Invalid format."));

        verifyNoInteractions(authService);
    }

    @Test
    void register_returns409_whenServiceReportsDuplicateEmail() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(ApiException.conflict("Email already exists."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dup@user.com\",\"name\":\"Dup\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists."));
    }

    @Test
    void registerRecruiter_returns201_andPassesCompanyChoice() throws Exception {
        mockMvc.perform(post("/auth/register-recruiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rec@acme.com\",\"name\":\"Rec\",\"password\":\"secret123\",\"companyId\":7}"))
                .andExpect(status().isCreated());

        verify(authService).registerRecruiter(argThat((RecruiterRegisterRequest request) ->
                request.getCompanyId() == 7L && request.getCompany() == null));
    }

    @Test
    void registerRecruiter_returns400_whenNewCompanyIsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/auth/register-recruiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rec@acme.com\",\"name\":\"Rec\",\"password\":\"secret123\","
                                + "\"company\":{\"name\":\"Acme\",\"industry\":\"TECHNOLOGY\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("company.size: Company size is required."));

        verifyNoInteractions(authService);
    }

    @Test
    void login_returnsTokens() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse("access", "refresh"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void login_returns401_whenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(ApiException.unauthorized("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_returns400_whenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body."));
    }

    @Test
    void refresh_passesTokenToService() throws Exception {
        when(authService.refresh("refresh-token")).thenReturn(new AuthResponse("new-access", "new-refresh"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void refresh_returns400_whenTokenIsBlank() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("refreshToken: Refresh token is required"));
    }

    @Test
    void logout_returns204_andRevokesTokensOfCurrentUser() throws Exception {
        mockMvc.perform(post("/auth/logout").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isNoContent());

        verify(authService).logout("candidate@test.com");
    }

    @Test
    void logout_returns401_withoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }
}
