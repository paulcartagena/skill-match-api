package com.paulcartagena.skillmatchapi.matching.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.matching.dto.CandidateMatchResponse;
import com.paulcartagena.skillmatchapi.matching.dto.JobMatchResponse;
import com.paulcartagena.skillmatchapi.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Matching")
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<JobMatchResponse> getJobMatches(@AuthenticationPrincipal User user) {
        return matchingService.getJobMatches(user);
    }

    @GetMapping("/jobs/{jobId}/candidates")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<CandidateMatchResponse> getCandidateMatches(@AuthenticationPrincipal User user,
                                                            @PathVariable Long jobId) {
        return matchingService.getCandidateMatches(user, jobId);
    }
}
