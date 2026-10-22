package com.paulcartagena.skillmatchapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Par de tokens de la sesión.")
public class AuthResponse {
    @Schema(description = "JWT para el header `Authorization: Bearer <accessToken>`. Dura 15 minutos.", example = "eyJhbGciOiJIUzM4NCJ9...")
    private String accessToken;
    @Schema(description = "Sirve para pedir un nuevo par de tokens en POST /auth/refresh. Dura 7 días. Cada login o refresh invalida el refresh token anterior del usuario.", example = "9b1f6c2e-4a7d-4e1b-8f3a-2c5d7e9a1b3c")
    private String refreshToken;
}