package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CandidateSkillRequest {

    @NotNull(message = "Skill level is required.")
    @Schema(description = "Nivel en la skill. Peso en el score de matching: BEGINNER 0.4, INTERMEDIATE 0.7, ADVANCED 0.9, EXPERT 1.0.", example = "ADVANCED")
    private SkillLevel level;
}
