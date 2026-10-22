package com.paulcartagena.skillmatchapi.application.controller;

import com.paulcartagena.skillmatchapi.application.dto.ApplicationResponse;
import com.paulcartagena.skillmatchapi.application.dto.ApplicationStatusRequest;
import com.paulcartagena.skillmatchapi.application.service.ApplicationService;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Applications")
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApplicationResponse apply(@AuthenticationPrincipal User user, @PathVariable Long jobId) {
        return applicationService.apply(user, jobId);
    }

    @GetMapping("/applications/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<ApplicationResponse> getMyApplications(@AuthenticationPrincipal User user) {
        return applicationService.getMyApplications(user);
    }

    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<ApplicationResponse> getJobApplications(@AuthenticationPrincipal User user,
                                                        @PathVariable Long jobId) {
        return applicationService.getJobApplications(user, jobId);
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse updateStatus(@AuthenticationPrincipal User user,
                                            @PathVariable Long id,
                                            @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(user, id, request.getStatus());
    }
}
