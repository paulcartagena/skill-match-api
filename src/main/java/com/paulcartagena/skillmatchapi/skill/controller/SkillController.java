package com.paulcartagena.skillmatchapi.skill.controller;

import com.paulcartagena.skillmatchapi.skill.dto.SkillRequest;
import com.paulcartagena.skillmatchapi.skill.dto.SkillResponse;
import com.paulcartagena.skillmatchapi.skill.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Skills", description = "Catálogo de skills que usan los perfiles de candidato y las ofertas. Solo el admin lo modifica.")
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear skill", description = "**Rol:** ADMIN.\n\n**Errores:** 400 datos inválidos · 409 ya existe una skill con ese nombre.")
    public SkillResponse create(@Valid @RequestBody SkillRequest request) {
        return skillService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar skill", description = "**Rol:** ADMIN.\n\n**Errores:** 400 datos inválidos · 404 skill inexistente.")
    public SkillResponse update(@Parameter(description = "ID de la skill.", example = "1") @PathVariable Long id, @Valid @RequestBody  SkillRequest request) {
        return skillService.update(id, request);
    }

    @GetMapping
    @Operation(summary = "Listar o buscar skills", description = "Devuelve todas las skills, o las que contienen el texto de `search` en el nombre (sin distinguir mayúsculas).\n\n**Rol:** cualquier usuario autenticado.")
    public List<SkillResponse> search(@Parameter(description = "Texto a buscar en el nombre. Opcional: sin texto devuelve todas.", example = "java") @RequestParam(required = false) String search) {
        return skillService.search(search);
    }

}
