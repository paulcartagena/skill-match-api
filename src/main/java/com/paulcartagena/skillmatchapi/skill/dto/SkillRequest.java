package com.paulcartagena.skillmatchapi.skill.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Skill del catálogo.")
public class SkillRequest {

    @NotBlank(message = "Name is required.")
    @Schema(description = "Nombre único de la skill.", example = "Docker")
    private String name;

    @NotNull(message = "Category is required.")
    @Schema(description = "Categoría.", example = "TOOL")
    private SkillCategory category;
}
