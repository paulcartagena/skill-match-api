package com.paulcartagena.skillmatchapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos del perfil del candidato. Las skills NO van aquí: se agregan una por una con PUT /api/candidates/me/skills/{skillId}.")
public class CandidateProfileRequest {

    @NotBlank(message = "Full name is required.")
    @Schema(description = "Nombre completo. Es el nombre que ven los reclutadores.", example = "Ana Torres")
    private String fullName;

    @Schema(description = "Presentación breve. Opcional.", example = "Backend developer con foco en APIs REST.")
    private String bio;

    @Schema(description = "Ubicación. Opcional.", example = "San Salvador")
    private String location;

    @Positive(message = "Years of experience must be positive.")
    @Schema(description = "Años de experiencia, mayor que 0. Opcional.", example = "3")
    private Integer yearsOfExperience;

    @Schema(description = "Teléfono. Opcional.", example = "7000-0000")
    private String phoneNumber;
}
