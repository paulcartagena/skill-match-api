package com.paulcartagena.skillmatchapi.auth.controller;

import com.paulcartagena.skillmatchapi.auth.dto.AuthResponse;
import com.paulcartagena.skillmatchapi.auth.dto.LoginRequest;
import com.paulcartagena.skillmatchapi.auth.dto.RefreshTokenRequest;
import com.paulcartagena.skillmatchapi.auth.dto.RecruiterRegisterRequest;
import com.paulcartagena.skillmatchapi.auth.dto.RegisterRequest;
import com.paulcartagena.skillmatchapi.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "Registro, login y manejo de tokens. Login y registro aceptan 5 intentos por minuto por IP (después responden 429).")
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar candidato", description = "Crea una cuenta de **candidato** y devuelve sus tokens: queda logueado.\n\nDespués debe completar su perfil con `PUT /api/candidates/me` antes de agregar skills, postular o ver el matching.\n\n**Errores:** 400 datos inválidos · 409 email ya registrado.")
    @SecurityRequirements
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/register-recruiter")
    @Operation(summary = "Registrar reclutador", description = "Crea una cuenta de **reclutador** junto con su perfil y su empresa. No devuelve tokens: la cuenta queda **pendiente** hasta que un admin la apruebe, y mientras tanto el login responde 403.\n\nEnvía `companyId` para unirte a una empresa existente (`GET /api/companies`) **o** `company` para crear una nueva, nunca los dos.\n\n**Errores:** 400 datos inválidos o ambas opciones de empresa · 404 companyId inexistente · 409 email ya registrado o nombre de empresa en uso.")
    @SecurityRequirements
    @ApiResponse(responseCode = "201", description = "Registrado. Pendiente de aprobación de un admin.")
    public ResponseEntity<Void> registerRecruiter(@Valid @RequestBody RecruiterRegisterRequest request) {
        authService.registerRecruiter(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Devuelve el par de tokens. Usa el `accessToken` en el header `Authorization: Bearer <accessToken>`.\n\n**Errores:** 401 credenciales incorrectas · 403 reclutador pendiente de aprobación o cuenta suspendida · 429 demasiados intentos.")
    @SecurityRequirements
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar tokens", description = "Cambia un refresh token válido por un **par nuevo**. Guarda los dos tokens nuevos: el refresh token anterior deja de servir.\n\nÚsalo cuando una llamada responda 401 por access token vencido (dura 15 minutos).\n\n**Errores:** 401 refresh token vencido o revocado: hay que volver a iniciar sesión.")
    @SecurityRequirements
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca los refresh tokens del usuario autenticado. El access token sigue siendo válido hasta que vence; el cliente debe descartarlo.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada.")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
