package com.paulcartagena.skillmatchapi.application.service;

import com.paulcartagena.skillmatchapi.application.dto.ApplicationResponse;
import com.paulcartagena.skillmatchapi.application.entity.Application;
import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import com.paulcartagena.skillmatchapi.application.repository.ApplicationRepository;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CandidateProfileRepository candidateProfileRepository,
                              JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.jobRepository = jobRepository;
    }

    public ApplicationResponse apply(User user, Long jobId) {
        CandidateProfile candidate = findCandidate(user);
        Job job = findJob(jobId);

        if (job.getStatus() != JobStatus.ACTIVE) {
            throw ApiException.badRequest("This job is not accepting applications.");
        }
        if (applicationRepository.existsByCandidateAndJob(candidate, job)) {
            throw ApiException.conflict("You have already applied to this job.");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);

        Application saved = applicationRepository.save(application);
        return buildResponse(saved);
    }

    public List<ApplicationResponse> getMyApplications(User user) {
        CandidateProfile candidate = findCandidate(user);

        return applicationRepository.findByCandidate(candidate)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public List<ApplicationResponse> getJobApplications(User user, Long jobId) {
        Job job = findJob(jobId);
        if (!isJobOwner(user, job)) {
            throw ApiException.forbidden("You can only view applications for your own jobs.");
        }

        return applicationRepository.findByJob(job)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public ApplicationResponse updateStatus(User user, Long applicationId, ApplicationStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ApiException.notFound("Application not found."));

        if (!isJobOwner(user, application.getJob())) {
            throw ApiException.forbidden("You can only manage applications for your own jobs.");
        }
        if (!application.getStatus().canTransitionTo(newStatus)) {
            throw ApiException.badRequest(
                    "Cannot change status from " + application.getStatus() + " to " + newStatus + ".");
        }

        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);
        return buildResponse(saved);
    }

    private CandidateProfile findCandidate(User user) {
        return candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.notFound("Profile not found. Complete your profile first."));
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Job not found."));
    }

    private boolean isJobOwner(User user, Job job) {
        return job.getRecruiter().getUser().getId().equals(user.getId());
    }

    private ApplicationResponse buildResponse(Application application) {
        Job job = application.getJob();
        CandidateProfile candidate = application.getCandidate();

        Set<String> candidateSkills = candidate.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        return new ApplicationResponse(
                application.getId(),
                job.getId(),
                job.getTitle(),
                job.getRecruiter().getCompanyName(),
                candidate.getId(),
                candidate.getFullName(),
                candidateSkills,
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}
