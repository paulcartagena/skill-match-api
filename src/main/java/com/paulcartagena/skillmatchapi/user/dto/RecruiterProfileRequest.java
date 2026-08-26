package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.user.enums.CompanySize;
import com.paulcartagena.skillmatchapi.user.enums.Industry;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecruiterProfileRequest {

    @NotBlank(message = "Company name is required.")
    private String companyName;

    private String companyWebsite;

    private Industry industry;

    private CompanySize companySize;

    private String jobTitle;
}