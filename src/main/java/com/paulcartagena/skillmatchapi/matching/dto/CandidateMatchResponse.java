package com.paulcartagena.skillmatchapi.matching.dto;

import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class CandidateMatchResponse {

    private Long candidateId;
    private String fullName;
    private String location;
    private Integer yearsOfExperience;
    private int score;
    private Set<CandidateSkillResponse> matchedSkills;
    private Set<String> missingRequiredSkills;
}
