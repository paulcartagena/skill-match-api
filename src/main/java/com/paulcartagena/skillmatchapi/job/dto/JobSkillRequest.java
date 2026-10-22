package com.paulcartagena.skillmatchapi.job.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillRequest {

    @NotNull(message = "Skill ID is required.")
    private Long skillId;

    @NotNull(message = "Required flag is required.")
    private Boolean required;
}
