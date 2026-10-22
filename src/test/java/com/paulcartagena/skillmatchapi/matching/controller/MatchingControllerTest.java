package com.paulcartagena.skillmatchapi.matching.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.matching.dto.CandidateMatchResponse;
import com.paulcartagena.skillmatchapi.matching.dto.JobMatchResponse;
import com.paulcartagena.skillmatchapi.matching.service.MatchingService;
import com.paulcartagena.skillmatchapi.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest extends ControllerTestSupport {

    @MockitoBean
    private MatchingService matchingService;

    @Test
    void getJobMatches_returnsRankedJobs_forCandidate() throws Exception {
        when(matchingService.getJobMatches(argThat((User user) -> user.getId() == 1L))).thenReturn(List.of(
                new JobMatchResponse(200L, "Senior Dev", "Acme Inc.", 85, Set.of(), Set.of()),
                new JobMatchResponse(100L, "Junior Dev", "Acme Inc.", 30, Set.of(), Set.of("Spring Boot"))));

        mockMvc.perform(get("/api/matching/jobs").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(85))
                .andExpect(jsonPath("$[1].missingRequiredSkills[0]").value("Spring Boot"));
    }

    @Test
    void getJobMatches_returns403_forRecruiter() throws Exception {
        mockMvc.perform(get("/api/matching/jobs").with(as(UserRole.RECRUITER)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matchingService);
    }

    @Test
    void getCandidateMatches_returnsRankedCandidates_forRecruiter() throws Exception {
        when(matchingService.getCandidateMatches(any(), eq(100L))).thenReturn(List.of(
                new CandidateMatchResponse(10L, "Ana Pérez", "Lima", 3, 70, Set.of(), Set.of())));

        mockMvc.perform(get("/api/matching/jobs/100/candidates").with(as(UserRole.RECRUITER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Ana Pérez"))
                .andExpect(jsonPath("$[0].score").value(70));
    }

    @Test
    void getCandidateMatches_returns403_whenRecruiterDoesNotOwnJob() throws Exception {
        when(matchingService.getCandidateMatches(any(), eq(100L)))
                .thenThrow(ApiException.forbidden("You can only view matches for your own jobs."));

        mockMvc.perform(get("/api/matching/jobs/100/candidates").with(as(UserRole.RECRUITER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only view matches for your own jobs."));
    }

    @Test
    void getCandidateMatches_returns403_forCandidate() throws Exception {
        mockMvc.perform(get("/api/matching/jobs/100/candidates").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matchingService);
    }
}
