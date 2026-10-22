package com.paulcartagena.skillmatchapi.skill.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Skill del catálogo.")
public class SkillResponse {

    @Schema(description = "ID de la skill.", example = "1")
    private Long id;
    @Schema(description = "Nombre.", example = "Java")
    private String name;
    @Schema(description = "Categoría.", example = "TECHNICAL")
    private SkillCategory category;
}
