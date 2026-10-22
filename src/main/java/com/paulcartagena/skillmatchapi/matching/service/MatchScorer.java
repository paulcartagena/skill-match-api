package com.paulcartagena.skillmatchapi.matching.service;

import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.entity.JobSkill;
import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.entity.CandidateSkill;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Scores how well a candidate fits a job, from 0 to 100.
 * Required skills weigh 70% and desired skills 30% (required take 100% when the job has no desired skills).
 * Each matched skill counts by the candidate's level, so an expert covers a skill fully and a beginner partially.
 */
@Component
public class MatchScorer {

    static final double REQUIRED_WEIGHT = 0.7;
    static final double DESIRED_WEIGHT = 0.3;

    private static final Map<SkillLevel, Double> LEVEL_WEIGHTS = Map.of(
            SkillLevel.BEGINNER, 0.4,
            SkillLevel.INTERMEDIATE, 0.7,
            SkillLevel.ADVANCED, 0.9,
            SkillLevel.EXPERT, 1.0
    );

    public MatchResult score(CandidateProfile candidate, Job job) {
        Map<Long, CandidateSkill> candidateSkills = candidate.getSkills().stream()
                .collect(Collectors.toMap(candidateSkill -> candidateSkill.getSkill().getId(), Function.identity()));

        List<JobSkill> required = job.getSkills().stream().filter(JobSkill::isRequired).toList();
        List<JobSkill> desired = job.getSkills().stream().filter(jobSkill -> !jobSkill.isRequired()).toList();

        Set<CandidateSkill> matched = new HashSet<>();
        Set<String> missingRequired = new HashSet<>();
        double requiredCoverage = coverage(required, candidateSkills, matched, missingRequired);
        double desiredCoverage = coverage(desired, candidateSkills, matched, new HashSet<>());

        double score = desired.isEmpty()
                ? requiredCoverage
                : REQUIRED_WEIGHT * requiredCoverage + DESIRED_WEIGHT * desiredCoverage;

        return new MatchResult((int) Math.round(score * 100), matched, missingRequired);
    }

    private double coverage(List<JobSkill> jobSkills,
                            Map<Long, CandidateSkill> candidateSkills,
                            Set<CandidateSkill> matched,
                            Set<String> missing) {
        if (jobSkills.isEmpty()) {
            return 0;
        }

        double total = 0;
        for (JobSkill jobSkill : jobSkills) {
            CandidateSkill candidateSkill = candidateSkills.get(jobSkill.getSkill().getId());
            if (candidateSkill == null) {
                missing.add(jobSkill.getSkill().getName());
            } else {
                matched.add(candidateSkill);
                total += LEVEL_WEIGHTS.get(candidateSkill.getLevel());
            }
        }
        return total / jobSkills.size();
    }

    public record MatchResult(int score, Set<CandidateSkill> matchedSkills, Set<String> missingRequiredSkills) {
    }
}
