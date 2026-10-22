package com.paulcartagena.skillmatchapi.job.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.job.dto.JobResponse;
import com.paulcartagena.skillmatchapi.job.dto.JobSkillRequest;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.entity.JobSkill;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final SkillRepository skillRepository;

    public JobService(JobRepository jobRepository,
                      RecruiterProfileRepository recruiterProfileRepository,
                      SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.skillRepository = skillRepository;
    }

    public JobResponse create(User user, JobRequest request) {
        RecruiterProfile recruiter = recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.notFound("Complete your recruiter profile first."));

        Job job = new Job();
        job.setRecruiter(recruiter);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setWorkMode(request.getWorkMode());
        job.setEmploymentType(request.getEmploymentType());
        applySkills(job, request.getSkills());

        Job saved = jobRepository.save(job);
        return buildResponse(saved);
    }

    public List<JobResponse> getJobs() {
        return jobRepository.
                findByStatus(JobStatus.ACTIVE)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public JobResponse getJob(User user, Long id) {
        Job job = findJob(id);

        // Closed/draft jobs are only visible to their owner; hide them from everyone else
        if (job.getStatus() != JobStatus.ACTIVE && !isOwner(user, job)) {
            throw ApiException.notFound("Job not found.");
        }
        return buildResponse(job);
    }

    public List<JobResponse> getMyJobs(User user) {
        RecruiterProfile recruiter = recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.notFound("Complete your recruiter profile first."));

        return jobRepository.findByRecruiter(recruiter)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public JobResponse update(User user, Long id, JobRequest request) {
        Job job = findOwnedJob(user, id);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw ApiException.badRequest("Closed jobs cannot be edited.");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setWorkMode(request.getWorkMode());
        job.setEmploymentType(request.getEmploymentType());
        applySkills(job, request.getSkills());

        Job saved = jobRepository.save(job);
        return buildResponse(saved);
    }

    public JobResponse close(User user, Long id) {
        Job job = findOwnedJob(user, id);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw ApiException.badRequest("Job is already closed.");
        }

        job.setStatus(JobStatus.CLOSED);
        Job saved = jobRepository.save(job);
        return buildResponse(saved);
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Job not found."));
    }

    private Job findOwnedJob(User user, Long id) {
        Job job = findJob(id);
        if (!isOwner(user, job)) {
            throw ApiException.forbidden("You can only modify your own jobs.");
        }
        return job;
    }

    private boolean isOwner(User user, Job job) {
        return job.getRecruiter().getUser().getId().equals(user.getId());
    }

    private void applySkills(Job job, List<JobSkillRequest> skillRequests) {
        Map<Long, Boolean> requiredBySkillId = new HashMap<>();
        for (JobSkillRequest skillRequest : skillRequests) {
            if (requiredBySkillId.put(skillRequest.getSkillId(), skillRequest.getRequired()) != null) {
                throw ApiException.badRequest("Each skill can only be listed once.");
            }
        }
        if (!requiredBySkillId.containsValue(true)) {
            throw ApiException.badRequest("At least one required skill is needed.");
        }

        List<Skill> foundSkills = skillRepository.findAllById(requiredBySkillId.keySet());
        if (foundSkills.size() != requiredBySkillId.size()) {
            throw ApiException.badRequest("One or more skill IDs do not exist.");
        }

        // Update existing rows in place instead of clearing the collection: Hibernate inserts before
        // it deletes orphans, so re-adding the same skill would hit the (job_id, skill_id) unique constraint
        job.getSkills().removeIf(jobSkill -> !requiredBySkillId.containsKey(jobSkill.getSkill().getId()));
        for (Skill skill : foundSkills) {
            boolean required = requiredBySkillId.get(skill.getId());
            job.getSkills().stream()
                    .filter(jobSkill -> jobSkill.getSkill().getId().equals(skill.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            jobSkill -> jobSkill.setRequired(required),
                            () -> job.getSkills().add(new JobSkill(null, job, skill, required))
                    );
        }
    }

    private Set<String> skillNames(Job job, boolean required) {
        return job.getSkills().stream()
                .filter(jobSkill -> jobSkill.isRequired() == required)
                .map(jobSkill -> jobSkill.getSkill().getName())
                .collect(Collectors.toSet());
    }

    private JobResponse buildResponse(Job job) {

        return new JobResponse(
                job.getId(),
                job.getRecruiter().getCompany().getName(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getWorkMode(),
                job.getEmploymentType(),
                job.getStatus(),
                skillNames(job, true),
                skillNames(job, false),
                job.getCreatedAt()
        );
    }
}
