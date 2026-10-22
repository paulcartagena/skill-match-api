package com.paulcartagena.skillmatchapi.matching.service;

import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.entity.JobSkill;
import com.paulcartagena.skillmatchapi.matching.service.MatchScorer.MatchResult;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.entity.CandidateSkill;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchScorerTest {

    private final MatchScorer matchScorer = new MatchScorer();

    private final Skill java = new Skill(1L, "Java", SkillCategory.TECHNICAL);
    private final Skill spring = new Skill(2L, "Spring Boot", SkillCategory.TECHNICAL);
    private final Skill docker = new Skill(3L, "Docker", SkillCategory.TOOL);

    @Test
    void score_is100_whenCandidateIsExpertInAllRequiredSkills_andJobHasNoDesiredSkills() {
        Job job = job(required(java), required(spring));
        CandidateProfile candidate = candidate(has(java, SkillLevel.EXPERT), has(spring, SkillLevel.EXPERT));

        MatchResult result = matchScorer.score(candidate, job);

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.missingRequiredSkills()).isEmpty();
    }

    @Test
    void score_isWeightedByCandidateLevel() {
        Job job = job(required(java));

        assertThat(matchScorer.score(candidate(has(java, SkillLevel.BEGINNER)), job).score()).isEqualTo(40);
        assertThat(matchScorer.score(candidate(has(java, SkillLevel.INTERMEDIATE)), job).score()).isEqualTo(70);
        assertThat(matchScorer.score(candidate(has(java, SkillLevel.ADVANCED)), job).score()).isEqualTo(90);
    }

    @Test
    void score_weighsRequired70AndDesired30() {
        Job job = job(required(java), required(spring), desired(docker));
        CandidateProfile candidate = candidate(has(java, SkillLevel.EXPERT), has(docker, SkillLevel.ADVANCED));

        MatchResult result = matchScorer.score(candidate, job);

        // required: (1.0 + 0) / 2 * 70 = 35, desired: 0.9 * 30 = 27
        assertThat(result.score()).isEqualTo(62);
        assertThat(result.missingRequiredSkills()).containsExactly("Spring Boot");
        assertThat(result.matchedSkills()).extracting(c -> c.getSkill().getName())
                .containsExactlyInAnyOrder("Java", "Docker");
    }

    @Test
    void score_capsAt70_whenOnlyRequiredSkillsMatch_andJobHasDesiredSkills() {
        Job job = job(required(java), desired(docker));
        CandidateProfile candidate = candidate(has(java, SkillLevel.EXPERT));

        assertThat(matchScorer.score(candidate, job).score()).isEqualTo(70);
    }

    @Test
    void score_is0_whenCandidateHasNoMatchingSkills() {
        Job job = job(required(java), desired(docker));
        CandidateProfile candidate = candidate(has(spring, SkillLevel.EXPERT));

        MatchResult result = matchScorer.score(candidate, job);

        assertThat(result.score()).isZero();
        assertThat(result.matchedSkills()).isEmpty();
        assertThat(result.missingRequiredSkills()).containsExactly("Java");
    }

    private JobSkill required(Skill skill) {
        return new JobSkill(null, null, skill, true);
    }

    private JobSkill desired(Skill skill) {
        return new JobSkill(null, null, skill, false);
    }

    private Job job(JobSkill... skills) {
        Job job = new Job();
        for (JobSkill jobSkill : skills) {
            jobSkill.setJob(job);
            job.getSkills().add(jobSkill);
        }
        return job;
    }

    private CandidateSkill has(Skill skill, SkillLevel level) {
        return new CandidateSkill(null, null, skill, level);
    }

    private CandidateProfile candidate(CandidateSkill... skills) {
        CandidateProfile candidate = new CandidateProfile();
        for (CandidateSkill candidateSkill : skills) {
            candidateSkill.setCandidate(candidate);
            candidate.getSkills().add(candidateSkill);
        }
        return candidate;
    }
}
