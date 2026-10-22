package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingRecruiterResponse {

    private Long userId;
    private String email;
    private String jobTitle;
    private CompanyResponse company;
}
