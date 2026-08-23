package com.paulcartagena.skillmatchapi.skill.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkillResponse {

    private Long id;
    private String name;
    private SkillCategory category;
}
