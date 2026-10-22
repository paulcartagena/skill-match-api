package com.paulcartagena.skillmatchapi.job.dto;

import com.paulcartagena.skillmatchapi.job.enums.EmploymentType;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JobRequest {

    @NotBlank(message = "Title is required.")
    private String title;

    @NotBlank(message = "Description is required.")
    private String description;

    private String location;

    @NotNull(message = "Work mode is required.")
    private WorkMode workMode;

    @NotNull(message = "Type is required.")
    private EmploymentType employmentType;

    @NotEmpty(message = "At least one skill is needed.")
    private List<@Valid JobSkillRequest> skills;
}