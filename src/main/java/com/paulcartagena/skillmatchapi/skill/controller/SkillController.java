package com.paulcartagena.skillmatchapi.skill.controller;

import com.paulcartagena.skillmatchapi.skill.dto.SkillRequest;
import com.paulcartagena.skillmatchapi.skill.dto.SkillResponse;
import com.paulcartagena.skillmatchapi.skill.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SkillResponse create(@Valid @RequestBody SkillRequest request) {
        return skillService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SkillResponse update(@PathVariable Long id, @Valid @RequestBody  SkillRequest request) {
        return skillService.update(id, request);
    }

    @GetMapping
    public List<SkillResponse> search(@RequestParam(required = false) String search) {
        return skillService.search(search);
    }

}
