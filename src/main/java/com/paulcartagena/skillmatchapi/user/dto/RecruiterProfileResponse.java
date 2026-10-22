package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecruiterProfileResponse {

    private Long id;
    private String jobTitle;
    private CompanyResponse company;
}
