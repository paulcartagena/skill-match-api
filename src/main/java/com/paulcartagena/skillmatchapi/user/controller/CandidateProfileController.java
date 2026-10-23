package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileResponse;
import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillRequest;
import com.paulcartagena.skillmatchapi.user.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Candidate Profile", description = "Perfil del candidato autenticado y sus skills con nivel. **Rol:** CANDIDATE.")
@RequestMapping("/api/candidates")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    public CandidateProfileController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }

    @GetMapping("/me")
    @Operation(summary = "Ver mi perfil", description = "Devuelve el perfil del candidato autenticado, con sus skills y niveles.\n\n**Errores:** 404 todavía no completó su perfil.")
    public CandidateProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return candidateProfileService.getMyProfile(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Crear o editar mi perfil", description = "Crea el perfil si no existe, o lo actualiza. Es el primer paso después del registro.\n\nLas skills **no** van aquí: se agregan con `PUT /api/candidates/me/skills/{skillId}`.\n\n**Errores:** 400 datos inválidos.")
    public CandidateProfileResponse updateMyProfile(@AuthenticationPrincipal User user,
                                                    @Valid @RequestBody CandidateProfileRequest request) {
        return candidateProfileService.createOrUpdate(user, request);
    }

    @PutMapping("/me/skills/{skillId}")
    @Operation(summary = "Agregar o cambiar una skill", description = "Agrega la skill al perfil con el nivel indicado; si ya la tenía, actualiza el nivel. El ID de la skill va en la URL y el body solo lleva el nivel.\n\nDevuelve el perfil completo actualizado.\n\n**Errores:** 400 nivel faltante o inválido · 404 skill o perfil inexistente.")
    public CandidateProfileResponse setSkill(@AuthenticationPrincipal User user,
                                             @Parameter(description = "ID de la skill (ver `GET /api/skills`).", example = "1") @PathVariable Long skillId,
                                             @Valid @RequestBody CandidateSkillRequest request) {
        return candidateProfileService.setSkill(user, skillId, request.getLevel());
    }

    @DeleteMapping("/me/skills/{skillId}")
    @Operation(summary = "Quitar una skill", description = "Quita la skill del perfil y devuelve el perfil actualizado.\n\n**Errores:** 404 skill o perfil inexistente.")
    public CandidateProfileResponse removeSkill(@AuthenticationPrincipal User user,
                                                @Parameter(description = "ID de la skill (ver `GET /api/skills`).", example = "1") @PathVariable Long skillId) {
        return candidateProfileService.removeSkill(user, skillId);
    }
}
