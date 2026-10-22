package com.paulcartagena.skillmatchapi.matching.dto;

import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class JobMatchResponse {

    private Long jobId;
    private String jobTitle;
    private String companyName;
    private int score;
    private Set<CandidateSkillResponse> matchedSkills;
    private Set<String> missingRequiredSkills;
}
