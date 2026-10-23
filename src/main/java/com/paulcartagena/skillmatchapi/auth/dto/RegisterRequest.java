package com.paulcartagena.skillmatchapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Registro de candidato.")
public class RegisterRequest {

    @NotBlank
    @Email(message = "Invalid format.")
    @Schema(description = "Email de la cuenta.", example = "ana@mail.com")
    private String email;

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
    @Schema(description = "Nombre, de 2 a 50 caracteres. Nota: hoy no se guarda; el nombre visible es el fullName del perfil (PUT /api/candidates/me).", example = "Ana Torres")
    private String name;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters.")
    @Schema(description = "Contraseña, de 6 a 72 caracteres.", example = "secret123")
    private String password;
}
