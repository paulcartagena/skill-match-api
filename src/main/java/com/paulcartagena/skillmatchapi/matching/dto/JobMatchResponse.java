package com.paulcartagena.skillmatchapi.matching.dto;

import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
@Schema(description = "Oferta recomendada para el candidato.")
public class JobMatchResponse {

    @Schema(description = "ID de la oferta.", example = "3")
    private Long jobId;
    @Schema(description = "Título de la oferta.", example = "Backend Developer Java")
    private String jobTitle;
    @Schema(description = "Empresa que publica la oferta.", example = "TechNova")
    private String companyName;
    @Schema(description = "Compatibilidad con la oferta, de 0 a 100.", example = "87")
    private int score;
    @Schema(description = "Skills del candidato que pide la oferta, con su nivel.")
    private Set<CandidateSkillResponse> matchedSkills;
    @Schema(description = "Skills requeridas por la oferta que el candidato no tiene.")
    private Set<String> missingRequiredSkills;
}
