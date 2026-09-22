package com.paulcartagena.skillmatchapi.job.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.job.dto.JobResponse;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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

        Set<Skill> requiredSkills = resolveSkills(request.getRequiredSkillIds());

        Job job = new Job();
        job.setRecruiter(recruiter);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setWorkMode(request.getWorkMode());
        job.setEmploymentType(request.getEmploymentType());
        job.setRequiredSkills(requiredSkills);

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

    private Set<Skill> resolveSkills(Set<Long> skillIds) {
        List<Skill> foundSkills = skillRepository.findAllById(skillIds);
        if (foundSkills.size() != skillIds.size()) {
            throw ApiException.badRequest("One or more required skill IDs do not exist.");
        }
        return new HashSet<>(foundSkills);
    }

    private JobResponse buildResponse(Job job) {
        Set<String> skillNames = job.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        return new JobResponse(
                job.getId(),
                job.getRecruiter().getCompanyName(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getWorkMode(),
                job.getEmploymentType(),
                job.getStatus(),
                skillNames,
                job.getCreatedAt()
        );
    }
}
