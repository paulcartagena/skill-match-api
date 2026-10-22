package com.paulcartagena.skillmatchapi.matching.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.matching.dto.CandidateMatchResponse;
import com.paulcartagena.skillmatchapi.matching.dto.JobMatchResponse;
import com.paulcartagena.skillmatchapi.matching.service.MatchScorer.MatchResult;
import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchScorer matchScorer;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    public MatchingService(MatchScorer matchScorer,
                           JobRepository jobRepository,
                           CandidateProfileRepository candidateProfileRepository) {
        this.matchScorer = matchScorer;
        this.jobRepository = jobRepository;
        this.candidateProfileRepository = candidateProfileRepository;
    }

    public List<JobMatchResponse> getJobMatches(User user) {
        CandidateProfile candidate = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.notFound("Profile not found. Complete your profile first."));

        return jobRepository.findWithSkillsByStatus(JobStatus.ACTIVE)
                .stream()
                .map(job -> buildJobMatch(job, matchScorer.score(candidate, job)))
                .sorted(Comparator.comparingInt(JobMatchResponse::getScore).reversed())
                .toList();
    }

    public List<CandidateMatchResponse> getCandidateMatches(User user, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Job not found."));
        if (!job.getRecruiter().getUser().getId().equals(user.getId())) {
            throw ApiException.forbidden("You can only view matches for your own jobs.");
        }

        return candidateProfileRepository.findAllWithSkills()
                .stream()
                .map(candidate -> buildCandidateMatch(candidate, matchScorer.score(candidate, job)))
                .sorted(Comparator.comparingInt(CandidateMatchResponse::getScore).reversed())
                .toList();
    }

    private JobMatchResponse buildJobMatch(Job job, MatchResult result) {
        return new JobMatchResponse(
                job.getId(),
                job.getTitle(),
                job.getRecruiter().getCompany().getName(),
                result.score(),
                toSkillResponses(result),
                result.missingRequiredSkills()
        );
    }

    private CandidateMatchResponse buildCandidateMatch(CandidateProfile candidate, MatchResult result) {
        return new CandidateMatchResponse(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getLocation(),
                candidate.getYearsOfExperience(),
                result.score(),
                toSkillResponses(result),
                result.missingRequiredSkills()
        );
    }

    private Set<CandidateSkillResponse> toSkillResponses(MatchResult result) {
        return result.matchedSkills().stream()
                .map(CandidateSkillResponse::from)
                .collect(Collectors.toSet());
    }
}
