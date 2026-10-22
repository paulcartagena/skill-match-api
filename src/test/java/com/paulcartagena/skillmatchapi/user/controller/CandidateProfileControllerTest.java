package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import com.paulcartagena.skillmatchapi.support.ControllerTestSupport;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileResponse;
import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import com.paulcartagena.skillmatchapi.user.service.CandidateProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandidateProfileController.class)
class CandidateProfileControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CandidateProfileService candidateProfileService;

    @Test
    void getMyProfile_returnsProfileOfCurrentCandidate() throws Exception {
        when(candidateProfileService.getMyProfile(argThat((User user) -> user.getId() == 1L)))
                .thenReturn(profile(Set.of(new CandidateSkillResponse(1L, "Java", SkillCategory.TECHNICAL, SkillLevel.EXPERT))));

        mockMvc.perform(get("/api/candidates/me").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ana Pérez"))
                .andExpect(jsonPath("$.skills[0].level").value("EXPERT"));
    }

    @Test
    void getMyProfile_returns404_whenProfileDoesNotExist() throws Exception {
        when(candidateProfileService.getMyProfile(any()))
                .thenThrow(ApiException.notFound("Profile not found. Complete your profile first."));

        mockMvc.perform(get("/api/candidates/me").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found. Complete your profile first."));
    }

    @Test
    void endpoints_return403_forRecruiters() throws Exception {
        mockMvc.perform(get("/api/candidates/me").with(as(UserRole.RECRUITER)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(candidateProfileService);
    }

    @Test
    void updateMyProfile_returns400_whenFullNameIsBlank() throws Exception {
        mockMvc.perform(put("/api/candidates/me").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fullName: Full name is required."));
    }

    @Test
    void updateMyProfile_returns400_whenYearsOfExperienceIsNegative() throws Exception {
        mockMvc.perform(put("/api/candidates/me").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana\",\"yearsOfExperience\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("yearsOfExperience: Years of experience must be positive."));
    }

    @Test
    void updateMyProfile_returnsUpdatedProfile() throws Exception {
        when(candidateProfileService.createOrUpdate(any(), any(CandidateProfileRequest.class))).thenReturn(profile(Set.of()));

        mockMvc.perform(put("/api/candidates/me").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana Pérez\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ana Pérez"));
    }

    @Test
    void setSkill_passesSkillIdAndLevel() throws Exception {
        when(candidateProfileService.setSkill(any(), eq(1L), eq(SkillLevel.ADVANCED))).thenReturn(profile(Set.of()));

        mockMvc.perform(put("/api/candidates/me/skills/1").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"ADVANCED\"}"))
                .andExpect(status().isOk());

        verify(candidateProfileService).setSkill(any(), eq(1L), eq(SkillLevel.ADVANCED));
    }

    @Test
    void setSkill_returns400_whenLevelIsMissing() throws Exception {
        mockMvc.perform(put("/api/candidates/me/skills/1").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("level: Skill level is required."));

        verifyNoInteractions(candidateProfileService);
    }

    @Test
    void setSkill_returns400_whenSkillIdIsNotANumber() throws Exception {
        mockMvc.perform(put("/api/candidates/me/skills/java").with(as(UserRole.CANDIDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"EXPERT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for 'skillId'."));
    }

    @Test
    void removeSkill_passesSkillId() throws Exception {
        when(candidateProfileService.removeSkill(any(), eq(1L))).thenReturn(profile(Set.of()));

        mockMvc.perform(delete("/api/candidates/me/skills/1").with(as(UserRole.CANDIDATE)))
                .andExpect(status().isOk());

        verify(candidateProfileService).removeSkill(any(), eq(1L));
    }

    private CandidateProfileResponse profile(Set<CandidateSkillResponse> skills) {
        return new CandidateProfileResponse(10L, "Ana Pérez", null, "Lima", 3, null, skills);
    }
}
