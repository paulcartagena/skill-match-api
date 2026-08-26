package com.paulcartagena.skillmatchapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CandidateProfileRequest {

    @NotBlank(message = "Full name is required.")
    private String fullName;

    private String bio;

    private String location;

    @Positive(message = "Years of experience must be positive.")
    private Integer yearsOfExperience;

    private String phoneNumber;
}
