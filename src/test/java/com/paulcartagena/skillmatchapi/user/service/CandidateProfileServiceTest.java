package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileResponse;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.repository.CandidateProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateProfileServiceTest {

    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private CandidateProfileService candidateProfileService;

    private User user;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void getMyProfile_throwsNotFound_whenProfileDoesNotExist() {
        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateProfileService.getMyProfile(user))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createOrUpdate_createsNewProfile_whenNoneExists() {
        CandidateProfileRequest request = new CandidateProfileRequest();
        request.setFullName("Jane Doe");

        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.empty());
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateProfileResponse response = candidateProfileService.createOrUpdate(user, request);

        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        verify(candidateProfileRepository).save(argThat(profile -> profile.getUser() == user));
    }

    @Test
    void createOrUpdate_updatesExistingProfile_insteadOfCreatingNew() {
        CandidateProfile existing = new CandidateProfile();
        existing.setId(5L);
        existing.setUser(user);
        existing.setFullName("Old Name");

        CandidateProfileRequest request = new CandidateProfileRequest();
        request.setFullName("New Name");

        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.of(existing));
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateProfileResponse response = candidateProfileService.createOrUpdate(user, request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getFullName()).isEqualTo("New Name");
    }

    @Test
    void addSkill_addsSkillToProfile() {
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        Skill skill = new Skill(1L, "Java", SkillCategory.TECHNICAL);

        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateProfileResponse response = candidateProfileService.addSkill(user, 1L);

        assertThat(response.getSkills()).extracting("name").containsExactly("Java");
    }

    @Test
    void addSkill_throwsNotFound_whenSkillDoesNotExist() {
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);

        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateProfileService.addSkill(user, 99L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeSkill_removesSkillFromProfile() {
        Skill skill = new Skill(1L, "Java", SkillCategory.TECHNICAL);
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.getSkills().add(skill);

        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateProfileResponse response = candidateProfileService.removeSkill(user, 1L);

        assertThat(response.getSkills()).isEmpty();
    }

    @Test
    void removeSkill_throwsNotFound_whenSkillDoesNotExist() {
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);

        when(candidateProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateProfileService.removeSkill(user, 99L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
