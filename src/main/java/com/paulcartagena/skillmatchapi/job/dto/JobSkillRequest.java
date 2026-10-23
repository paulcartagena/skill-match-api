package com.paulcartagena.skillmatchapi.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Skill de una oferta.")
public class JobSkillRequest {

    @NotNull(message = "Skill ID is required.")
    @Schema(description = "ID de la skill (ver GET /api/skills).", example = "1")
    private Long skillId;

    @NotNull(message = "Required flag is required.")
    @Schema(description = "true = requerida, false = deseada.", example = "true")
    private Boolean required;
}
