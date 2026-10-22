package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import com.paulcartagena.skillmatchapi.support.ControllerTestSupport;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
import com.paulcartagena.skillmatchapi.user.service.RecruiterProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecruiterProfileController.class)
class RecruiterProfileControllerTest extends ControllerTestSupport {

    @MockitoBean
    private RecruiterProfileService recruiterProfileService;

    private final RecruiterProfileResponse profile = new RecruiterProfileResponse(
            5L, "Tech Lead", new CompanyResponse(7L, "Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.SMALL));

    @Test
    void getMyProfile_returnsProfileWithCompany() throws Exception {
        when(recruiterProfileService.getMyProfile(any())).thenReturn(profile);

        mockMvc.perform(get("/api/recruiters/me").with(as(UserRole.RECRUITER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Tech Lead"))
                .andExpect(jsonPath("$.company.name").value("Acme Inc."));
    }

    @Test
    void updateMyProfile_updatesJobTitle() throws Exception {
        when(recruiterProfileService.update(any(), any(RecruiterProfileRequest.class))).thenReturn(profile);

        mockMvc.perform(put("/api/recruiters/me").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Tech Lead\"}"))
                .andExpect(status().isOk());

        verify(recruiterProfileService).update(any(), argThat(request -> "Tech Lead".equals(request.getJobTitle())));
    }

    @Test
    void endpoints_return403_forCandidates() throws Exception {
        mockMvc.perform(get("/api/recruiters/me").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recruiterProfileService);
    }

    @Test
    void endpoints_return401_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/recruiters/me"))
                .andExpect(status().isUnauthorized());
    }
}
