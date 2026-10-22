package com.paulcartagena.skillmatchapi.application.dto;

import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Postulación de un candidato a una oferta.")
public class ApplicationResponse {

    @Schema(description = "ID de la postulación.", example = "1")
    private Long id;
    @Schema(description = "ID de la oferta.", example = "3")
    private Long jobId;
    @Schema(description = "Título de la oferta.", example = "Backend Developer Java")
    private String jobTitle;
    @Schema(description = "Empresa que publica la oferta.", example = "TechNova")
    private String companyName;
    @Schema(description = "ID del perfil del candidato.", example = "10")
    private Long candidateId;
    @Schema(description = "Nombre completo del candidato (fullName de su perfil).", example = "Ana Torres")
    private String candidateName;
    @Schema(description = "Skills del candidato con su nivel.")
    private Set<CandidateSkillResponse> candidateSkills;
    @Schema(description = "Estado actual de la postulación.", example = "APPLIED")
    private ApplicationStatus status;
    @Schema(description = "Fecha y hora de la postulación.", example = "2026-10-22T18:30:00")
    private LocalDateTime createdAt;
}
