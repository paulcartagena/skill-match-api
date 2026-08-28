package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
import com.paulcartagena.skillmatchapi.user.service.RecruiterProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Recruiter Profile")
@RequestMapping("/api/recruiters")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;

    public RecruiterProfileController(RecruiterProfileService recruiterProfileService) {
        this.recruiterProfileService = recruiterProfileService;
    }

    @GetMapping("/me")
    public RecruiterProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return recruiterProfileService.getMyProfile(user);
    }

    @PutMapping("/me")
    public RecruiterProfileResponse updateMyProfile(@AuthenticationPrincipal User user,
                                                    @Valid @RequestBody RecruiterProfileRequest request) {
        return recruiterProfileService.createOrUpdate(user, request);
    }
}