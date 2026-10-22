package com.paulcartagena.skillmatchapi.skill.controller;

import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.skill.dto.SkillRequest;
import com.paulcartagena.skillmatchapi.skill.dto.SkillResponse;
import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.service.SkillService;
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

@WebMvcTest(SkillController.class)
class SkillControllerTest extends ControllerTestSupport {

    @MockitoBean
    private SkillService skillService;

    @Test
    void search_returnsSkills_forAnyAuthenticatedUser() throws Exception {
        when(skillService.search("ja")).thenReturn(List.of(new SkillResponse(1L, "Java", SkillCategory.TECHNICAL)));

        mockMvc.perform(get("/api/skills").param("search", "ja").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].category").value("TECHNICAL"));
    }

    @Test
    void search_returns401_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required or token invalid/expired."));

        verifyNoInteractions(skillService);
    }

    @Test
    void create_returnsSkill_forAdmin() throws Exception {
        when(skillService.create(any(SkillRequest.class)))
                .thenReturn(new SkillResponse(5L, "Docker", SkillCategory.TOOL));

        mockMvc.perform(post("/api/skills").with(as(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Docker\",\"category\":\"TOOL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void create_returns403_forNonAdmin() throws Exception {
        mockMvc.perform(post("/api/skills").with(as(UserRole.RECRUITER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Docker\",\"category\":\"TOOL\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You don't have permission to access this resource."));

        verifyNoInteractions(skillService);
    }

    @Test
    void create_returns400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/skills").with(as(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"category\":\"TOOL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: Name is required."));

        verifyNoInteractions(skillService);
    }

    @Test
    void create_returns400_whenCategoryIsUnknown() throws Exception {
        mockMvc.perform(post("/api/skills").with(as(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Docker\",\"category\":\"VIBES\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for 'category'."));
    }

    @Test
    void update_passesIdToService_forAdmin() throws Exception {
        when(skillService.update(eq(5L), any(SkillRequest.class)))
                .thenReturn(new SkillResponse(5L, "Docker", SkillCategory.TOOL));

        mockMvc.perform(put("/api/skills/5").with(as(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Docker\",\"category\":\"TOOL\"}"))
                .andExpect(status().isOk());

        verify(skillService).update(eq(5L), any(SkillRequest.class));
    }
}
