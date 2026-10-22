package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.support.ControllerTestSupport;
import com.paulcartagena.skillmatchapi.user.dto.PendingRecruiterResponse;
import com.paulcartagena.skillmatchapi.user.service.AdminRecruiterService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminRecruiterController.class)
class AdminRecruiterControllerTest extends ControllerTestSupport {

    @MockitoBean
    private AdminRecruiterService adminRecruiterService;

    @Test
    void getPendingRecruiters_returnsRecruitersWithTheirCompany() throws Exception {
        when(adminRecruiterService.getPendingRecruiters()).thenReturn(List.of(new PendingRecruiterResponse(
                4L, "rec@acme.com", "Tech Lead",
                new CompanyResponse(7L, "Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.SMALL))));

        mockMvc.perform(get("/api/admin/recruiters/pending").with(as(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("rec@acme.com"))
                .andExpect(jsonPath("$[0].company.name").value("Acme Inc."));
    }

    @Test
    void approveRecruiter_approvesByUserId() throws Exception {
        mockMvc.perform(patch("/api/admin/recruiters/4/approve").with(as(UserRole.ADMIN)))
                .andExpect(status().isOk());

        verify(adminRecruiterService).approveRecruiter(4L);
    }

    @Test
    void approveRecruiter_returns400_whenUserIsNotRecruiter() throws Exception {
        doThrow(ApiException.badRequest("User is not a recruiter.")).when(adminRecruiterService).approveRecruiter(1L);

        mockMvc.perform(patch("/api/admin/recruiters/1/approve").with(as(UserRole.ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not a recruiter."));
    }

    @Test
    void endpoints_return403_forNonAdmins() throws Exception {
        mockMvc.perform(get("/api/admin/recruiters/pending").with(as(UserRole.RECRUITER)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/admin/recruiters/4/approve").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminRecruiterService);
    }
}
