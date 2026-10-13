package com.paulcartagena.skillmatchapi.job.dto;

import com.paulcartagena.skillmatchapi.job.enums.EmploymentType;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private Long id;
    private String companyName;
    private String title;
    private String description;
    private String location;
    private WorkMode workMode;
    private EmploymentType employmentType;
    private JobStatus status;
    private Set<String> requiredSkills;
    private LocalDateTime createdAt;
}
