package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileResponse;
import com.paulcartagena.skillmatchapi.user.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Candidate Profile")
@RequestMapping("/api/candidates")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    public CandidateProfileController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }

    @GetMapping("/me")
    public CandidateProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return candidateProfileService.getMyProfile(user);
    }

    @PutMapping("/me")
    public CandidateProfileResponse updateMyProfile(@AuthenticationPrincipal User user,
                                                    @Valid @RequestBody CandidateProfileRequest request) {
        return candidateProfileService.createOrUpdate(user, request);
    }

    @PostMapping("/me/skills/{skillId}")
    public CandidateProfileResponse addSkill(@AuthenticationPrincipal User user,
                                             @PathVariable Long skillId) {
        return candidateProfileService.addSkill(user, skillId);
    }

    @DeleteMapping("/me/skills/{skillId}")
    public CandidateProfileResponse removeSkill(@AuthenticationPrincipal User user,
                                                @PathVariable Long skillId) {
        return candidateProfileService.removeSkill(user, skillId);
    }
}
