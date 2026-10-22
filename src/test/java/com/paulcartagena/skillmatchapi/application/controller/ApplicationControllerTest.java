package com.paulcartagena.skillmatchapi.application.controller;

import com.paulcartagena.skillmatchapi.application.dto.ApplicationResponse;
import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import com.paulcartagena.skillmatchapi.application.service.ApplicationService;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.exception.ApiException;
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

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void apply_createsApplication_forCandidate() throws Exception {
        when(applicationService.apply(any(), eq(100L))).thenReturn(application(ApplicationStatus.APPLIED));

        mockMvc.perform(post("/api/jobs/100/applications").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(100))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void apply_returns403_forRecruiter() throws Exception {
        mockMvc.perform(post("/api/jobs/100/applications").with(as(UserRole.RECRUITER)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    @Test
    void apply_returns409_whenAlreadyApplied() throws Exception {
        when(applicationService.apply(any(), eq(100L)))
                .thenThrow(ApiException.conflict("You have already applied to this job."));

        mockMvc.perform(post("/api/jobs/100/applications").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You have already applied to this job."));
    }

    @Test
    void getMyApplications_returnsApplications_forCandidate() throws Exception {
        when(applicationService.getMyApplications(any())).thenReturn(List.of(application(ApplicationStatus.APPLIED)));

        mockMvc.perform(get("/api/applications/me").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getJobApplications_returns403_forCandidate() throws Exception {
        mockMvc.perform(get("/api/jobs/100/applications").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getJobApplications_returnsApplications_forRecruiter() throws Exception {
        when(applicationService.getJobApplications(any(), eq(100L))).thenReturn(List.of(application(ApplicationStatus.APPLIED)));

        mockMvc.perform(get("/api/jobs/100/applications").with(as(UserRole.RECRUITER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateName").value("Ana Pérez"));
    }

    @Test
    void updateStatus_passesNewStatus() throws Exception {
        when(applicationService.updateStatus(any(), eq(1L), eq(ApplicationStatus.REVIEWED)))
                .thenReturn(application(ApplicationStatus.REVIEWED));

        mockMvc.perform(patch("/api/applications/1/status").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REVIEWED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"));
    }

    @Test
    void updateStatus_returns400_whenStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/api/applications/1/status").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status: Status is required."));

        verifyNoInteractions(applicationService);
    }

    @Test
    void updateStatus_returns400_whenTransitionIsInvalid() throws Exception {
        when(applicationService.updateStatus(any(), eq(1L), eq(ApplicationStatus.APPLIED)))
                .thenThrow(ApiException.badRequest("Cannot change status from REJECTED to APPLIED."));

        mockMvc.perform(patch("/api/applications/1/status").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot change status from REJECTED to APPLIED."));
    }

    private ApplicationResponse application(ApplicationStatus status) {
        return new ApplicationResponse(1L, 100L, "Backend Developer", "Acme Inc.", 10L, "Ana Pérez", Set.of(), status, null);
    }
}
