package com.paulcartagena.skillmatchapi.skill.service;

import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.skill.dto.SkillRequest;
import com.paulcartagena.skillmatchapi.skill.dto.SkillResponse;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.enums.SkillCategory;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    void create_savesSkill_whenNameIsNew() {
        SkillRequest request = new SkillRequest();
        request.setName("Java");
        request.setCategory(SkillCategory.TECHNICAL);

        when(skillRepository.findByName("Java")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            skill.setId(1L);
            return skill;
        });

        SkillResponse response = skillService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Java");
        assertThat(response.getCategory()).isEqualTo(SkillCategory.TECHNICAL);
    }

    @Test
    void create_throwsConflict_whenNameAlreadyExists() {
        SkillRequest request = new SkillRequest();
        request.setName("Java");
        request.setCategory(SkillCategory.TECHNICAL);

        when(skillRepository.findByName("Java")).thenReturn(Optional.of(new Skill()));

        assertThatThrownBy(() -> skillService.create(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(skillRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFound_whenSkillDoesNotExist() {
        SkillRequest request = new SkillRequest();
        request.setName("Java");
        request.setCategory(SkillCategory.TECHNICAL);

        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.update(99L, request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void search_returnsAllSkills_whenQueryIsBlank() {
        when(skillRepository.findAll()).thenReturn(List.of(
                new Skill(1L, "Java", SkillCategory.TECHNICAL),
                new Skill(2L, "Communication", SkillCategory.SOFT_SKILL)
        ));

        List<SkillResponse> results = skillService.search(" ");

        assertThat(results).hasSize(2);
        verify(skillRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void search_filtersByName_whenQueryIsProvided() {
        when(skillRepository.findByNameContainingIgnoreCase("java"))
                .thenReturn(List.of(new Skill(1L, "Java", SkillCategory.TECHNICAL)));

        List<SkillResponse> results = skillService.search("java");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Java");
        verify(skillRepository, never()).findAll();
    }
}
