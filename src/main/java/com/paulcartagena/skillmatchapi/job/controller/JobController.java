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
}
