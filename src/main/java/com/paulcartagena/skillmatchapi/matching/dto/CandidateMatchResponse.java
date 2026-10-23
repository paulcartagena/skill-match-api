package com.paulcartagena.skillmatchapi.matching.dto;

import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
@Schema(description = "Candidato rankeado para una oferta.")
public class CandidateMatchResponse {

    @Schema(description = "ID del perfil del candidato.", example = "10")
    private Long candidateId;
    @Schema(description = "Nombre completo del candidato.", example = "Ana Torres")
    private String fullName;
    @Schema(description = "Ubicación del candidato.", example = "San Salvador")
    private String location;
    @Schema(description = "Años de experiencia.", example = "3")
    private Integer yearsOfExperience;
    @Schema(description = "Compatibilidad con la oferta, de 0 a 100.", example = "87")
    private int score;
    @Schema(description = "Skills del candidato que pide la oferta, con su nivel.")
    private Set<CandidateSkillResponse> matchedSkills;
    @Schema(description = "Skills requeridas por la oferta que el candidato no tiene.")
    private Set<String> missingRequiredSkills;
}
