package com.paulcartagena.skillmatchapi.application.dto;

import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long candidateId;
    private String candidateName;
    private Set<String> candidateSkills;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
}
