package com.paulcartagena.skillmatchapi.job.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.job.dto.JobResponse;
import com.paulcartagena.skillmatchapi.job.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Jobs")
@RequestMapping("api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse create(@AuthenticationPrincipal User user,
                              @Valid @RequestBody JobRequest request) {
        return jobService.create(user, request);
    }

    @GetMapping
    public List<JobResponse> getJobs() {
        return jobService.getJobs();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<JobResponse> getMyJobs(@AuthenticationPrincipal User user) {
        return jobService.getMyJobs(user);
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return jobService.getJob(user, id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse update(@AuthenticationPrincipal User user,
                              @PathVariable Long id,
                              @Valid @RequestBody JobRequest request) {
        return jobService.update(user, id, request);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse close(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return jobService.close(user, id);
    }
}
