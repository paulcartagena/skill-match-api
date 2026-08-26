package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.user.enums.CompanySize;
import com.paulcartagena.skillmatchapi.user.enums.Industry;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecruiterProfileResponse {

    private Long id;
    private String companyName;
    private String companyWebsite;
    private Industry industry;
    private CompanySize companySize;
    private String jobTitle;
}
