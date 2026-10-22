package com.paulcartagena.skillmatchapi.job.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.job.dto.JobResponse;
import com.paulcartagena.skillmatchapi.job.enums.EmploymentType;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import com.paulcartagena.skillmatchapi.job.service.JobService;
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

@WebMvcTest(JobController.class)
class JobControllerTest extends ControllerTestSupport {

    private static final String VALID_JOB = """
            {"title":"Backend Developer","description":"Build APIs","workMode":"REMOTE","employmentType":"FULL_TIME","skills":[{"skillId":1,"required":true}]}""";

    @MockitoBean
    private JobService jobService;

    @Test
    void create_returnsJob_forRecruiter() throws Exception {
        when(jobService.create(any(), any(JobRequest.class))).thenReturn(job(JobStatus.ACTIVE));

        mockMvc.perform(post("/api/jobs").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.requiredSkills[0]").value("Java"))
                .andExpect(jsonPath("$.desiredSkills[0]").value("Docker"));

        verify(jobService).create(argThat((User user) -> user.getId() == 2L), argThat(request ->
                request.getSkills().getFirst().getSkillId() == 1L && request.getSkills().getFirst().getRequired()));
    }

    @Test
    void create_returns403_forCandidate() throws Exception {
        mockMvc.perform(post("/api/jobs").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jobService);
    }

    @Test
    void create_returns400_whenSkillsAreEmpty() throws Exception {
        mockMvc.perform(post("/api/jobs").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB.replace("[{\"skillId\":1,\"required\":true}]", "[]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("skills: At least one skill is needed."));
    }

    @Test
    void create_returns400_whenSkillRequiredFlagIsMissing() throws Exception {
        mockMvc.perform(post("/api/jobs").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB.replace(",\"required\":true", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("skills[0].required: Required flag is required."));
    }

    @Test
    void create_returns400_whenWorkModeIsUnknown() throws Exception {
        mockMvc.perform(post("/api/jobs").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB.replace("REMOTE", "ON_THE_MOON")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for 'workMode'."));
    }

    @Test
    void getJobs_returnsActiveJobs_forAnyAuthenticatedUser() throws Exception {
        when(jobService.getJobs()).thenReturn(List.of(job(JobStatus.ACTIVE)));

        mockMvc.perform(get("/api/jobs").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void getJobs_returns401_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyJobs_returns403_forCandidate() throws Exception {
        mockMvc.perform(get("/api/jobs/me").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getJob_returns404_whenServiceHidesTheJob() throws Exception {
        when(jobService.getJob(any(), eq(100L))).thenThrow(ApiException.notFound("Job not found."));

        mockMvc.perform(get("/api/jobs/100").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job not found."));
    }

    @Test
    void getJob_returns400_whenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/jobs/abc").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for 'id'."));
    }

    @Test
    void update_returns403_whenServiceRejectsNonOwner() throws Exception {
        when(jobService.update(any(), eq(100L), any(JobRequest.class)))
                .thenThrow(ApiException.forbidden("You can only modify your own jobs."));

        mockMvc.perform(put("/api/jobs/100").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only modify your own jobs."));
    }

    @Test
    void close_returnsClosedJob() throws Exception {
        when(jobService.close(any(), eq(100L))).thenReturn(job(JobStatus.CLOSED));

        mockMvc.perform(patch("/api/jobs/100/close").with(as(UserRole.RECRUITER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void close_returns405_forWrongMethod() throws Exception {
        mockMvc.perform(post("/api/jobs/100/close").with(as(UserRole.RECRUITER)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "PATCH"));
    }

    private JobResponse job(JobStatus status) {
        return new JobResponse(100L, "Acme Inc.", "Backend Developer", "Build APIs", "Remote",
                WorkMode.REMOTE, EmploymentType.FULL_TIME, status, Set.of("Java"), Set.of("Docker"), null);
    }
}
