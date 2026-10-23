package com.paulcartagena.skillmatchapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "El refreshToken recibido en el login o en el último refresh.", example = "9b1f6c2e-4a7d-4e1b-8f3a-2c5d7e9a1b3c")
    private String refreshToken;
}
