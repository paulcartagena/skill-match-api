package com.paulcartagena.skillmatchapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
@Schema(description = "Perfil del candidato.")
public class CandidateProfileResponse {
    @Schema(description = "ID del perfil del candidato.", example = "10")
    private Long id;
    @Schema(description = "Nombre completo.", example = "Ana Torres")
    private String fullName;
    @Schema(description = "Presentación breve.", example = "Backend developer con foco en APIs REST.")
    private String bio;
    @Schema(description = "Ubicación.", example = "San Salvador")
    private String location;
    @Schema(description = "Años de experiencia.", example = "3")
    private Integer yearsOfExperience;
    @Schema(description = "Teléfono.", example = "7000-0000")
    private String phoneNumber;
    @Schema(description = "Skills del candidato con su nivel.")
    private Set<CandidateSkillResponse> skills;
}
