package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import com.paulcartagena.skillmatchapi.user.entity.CandidateSkill;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CandidateSkillResponse {

    private Long skillId;
    private String name;
    private SkillCategory category;
    private SkillLevel level;

    public static CandidateSkillResponse from(CandidateSkill candidateSkill) {
        return new CandidateSkillResponse(
                candidateSkill.getSkill().getId(),
                candidateSkill.getSkill().getName(),
                candidateSkill.getSkill().getCategory(),
                candidateSkill.getLevel()
        );
    }
}
