package com.paulcartagena.skillmatchapi.skill.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotNull(message = "Category is required.")
    private SkillCategory category;
}
