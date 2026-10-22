package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import com.paulcartagena.skillmatchapi.user.entity.CandidateSkill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Skill de un candidato con su nivel.")
public class CandidateSkillResponse {

    @Schema(description = "ID de la skill.", example = "1")
    private Long skillId;
    @Schema(description = "Nombre de la skill.", example = "Java")
    private String name;
    @Schema(description = "Categoría.", example = "TECHNICAL")
    private SkillCategory category;
    @Schema(description = "Nivel del candidato en la skill.", example = "ADVANCED")
    private SkillLevel level;

    public static CandidateSkillResponse from(CandidateSkill candidateSkill) {
        return new CandidateSkillResponse(
                candidateSkill.getSkill().getId(),
                candidateSkill.getSkill().getName(),
                candidateSkill.getSkill().getCategory(),
                candidateSkill.getLevel()
        );
    }
}
