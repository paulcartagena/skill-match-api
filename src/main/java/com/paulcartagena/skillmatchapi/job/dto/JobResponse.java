package com.paulcartagena.skillmatchapi.job.dto;

import com.paulcartagena.skillmatchapi.job.enums.EmploymentType;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Oferta de trabajo.")
public class JobResponse {

    @Schema(description = "ID de la oferta.", example = "3")
    private Long id;
    @Schema(description = "Empresa que publica la oferta.", example = "TechNova")
    private String companyName;
    @Schema(description = "Título.", example = "Backend Developer Java")
    private String title;
    @Schema(description = "Descripción del puesto.", example = "Desarrollo de APIs REST con Spring Boot y PostgreSQL.")
    private String description;
    @Schema(description = "Ubicación.", example = "San Salvador")
    private String location;
    @Schema(description = "Modalidad de trabajo.", example = "REMOTE")
    private WorkMode workMode;
    @Schema(description = "Tipo de contrato.", example = "FULL_TIME")
    private EmploymentType employmentType;
    @Schema(description = "ACTIVE: visible para todos. CLOSED: cerrada, solo la ve su reclutador.", example = "ACTIVE")
    private JobStatus status;
    @Schema(description = "Nombres de las skills requeridas.")
    private Set<String> requiredSkills;
    @Schema(description = "Nombres de las skills deseadas.")
    private Set<String> desiredSkills;
    @Schema(description = "Fecha de publicación.", example = "2026-10-22T18:00:00")
    private LocalDateTime createdAt;
}
