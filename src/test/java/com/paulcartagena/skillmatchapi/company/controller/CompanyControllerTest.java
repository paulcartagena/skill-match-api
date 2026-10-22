package com.paulcartagena.skillmatchapi.company.controller;

import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import com.paulcartagena.skillmatchapi.company.service.CompanyService;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.support.ControllerTestSupport;
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

@WebMvcTest(CompanyController.class)
class CompanyControllerTest extends ControllerTestSupport {

    private static final String VALID_COMPANY = "{\"name\":\"Acme Inc.\",\"industry\":\"TECHNOLOGY\",\"size\":\"SMALL\"}";

    @MockitoBean
    private CompanyService companyService;

    private final CompanyResponse acme = new CompanyResponse(7L, "Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.SMALL);

    @Test
    void getCompanies_isPublic() throws Exception {
        when(companyService.getCompanies()).thenReturn(List.of(acme));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Acme Inc."));
    }

    @Test
    void getCompany_isPublic() throws Exception {
        when(companyService.getCompany(7L)).thenReturn(acme);

        mockMvc.perform(get("/api/companies/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value("SMALL"));
    }

    @Test
    void getCompany_returns404_whenCompanyDoesNotExist() throws Exception {
        when(companyService.getCompany(99L)).thenThrow(ApiException.notFound("Company not found."));

        mockMvc.perform(get("/api/companies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found."));
    }

    @Test
    void update_returns401_withoutAuthentication() throws Exception {
        mockMvc.perform(put("/api/companies/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(companyService);
    }

    @Test
    void update_returns403_forCandidate() throws Exception {
        mockMvc.perform(put("/api/companies/7").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(companyService);
    }

    @Test
    void update_returnsCompany_forRecruiter() throws Exception {
        when(companyService.update(any(), eq(7L), any(CompanyRequest.class))).thenReturn(acme);

        mockMvc.perform(put("/api/companies/7").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Inc."));
    }

    @Test
    void update_returns400_whenIndustryIsMissing() throws Exception {
        mockMvc.perform(put("/api/companies/7").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme Inc.\",\"size\":\"SMALL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("industry: Industry is required."));
    }

    @Test
    void update_returns409_whenNameIsTaken() throws Exception {
        when(companyService.update(any(), eq(7L), any(CompanyRequest.class)))
                .thenThrow(ApiException.conflict("A company with this name already exists."));

        mockMvc.perform(put("/api/companies/7").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isConflict());
    }
}
