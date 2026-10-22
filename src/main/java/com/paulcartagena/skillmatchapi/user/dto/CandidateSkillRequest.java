package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CandidateSkillRequest {

    @NotNull(message = "Skill level is required.")
    private SkillLevel level;
}
