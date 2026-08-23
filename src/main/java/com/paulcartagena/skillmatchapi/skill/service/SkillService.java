package com.paulcartagena.skillmatchapi.skill.service;

import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.skill.dto.SkillRequest;
import com.paulcartagena.skillmatchapi.skill.dto.SkillResponse;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public SkillResponse create(SkillRequest request) {
        if (skillRepository.findByName(request.getName()).isPresent()) {
            throw ApiException.conflict("Skill already exists");
        }

        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());

        Skill saved = skillRepository.save(skill);
        return buildResponse(saved);
    }

    public SkillResponse update(Long id, SkillRequest request) {
       Skill skill = skillRepository.findById(id)
               .orElseThrow(() -> ApiException.notFound("Skill not found"));

       skill.setName(request.getName());
       skill.setCategory(request.getCategory());

       Skill updated = skillRepository.save(skill);
       return buildResponse(updated);
    }

    public List<SkillResponse> search(String query) {
        List<Skill> skills = (query == null || query.isBlank())
                ? skillRepository.findAll()
                : skillRepository.findByNameContainingIgnoreCase(query);

        return skills.stream()
                .map(this::buildResponse)
                .toList();
    }

    private SkillResponse buildResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory()
        );
    }
}
