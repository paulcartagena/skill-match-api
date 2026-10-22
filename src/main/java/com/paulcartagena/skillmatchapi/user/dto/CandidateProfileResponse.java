package com.paulcartagena.skillmatchapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class CandidateProfileResponse {
    private Long id;
    private String fullName;
    private String bio;
    private String location;
    private Integer yearsOfExperience;
    private String phoneNumber;
    private Set<CandidateSkillResponse> skills;
}
