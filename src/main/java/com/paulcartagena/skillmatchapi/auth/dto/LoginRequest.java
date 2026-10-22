package com.paulcartagena.skillmatchapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    @Email(message = "Invalid format.")
    @Schema(description = "Email de la cuenta.", example = "ana@mail.com")
    private String email;

    @NotBlank
    @Schema(description = "Contraseña.", example = "secret123")
    private String password;
}