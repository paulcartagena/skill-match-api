package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
import com.paulcartagena.skillmatchapi.user.service.RecruiterProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Recruiter Profile", description = "Perfil del reclutador autenticado. Se crea al registrarse. **Rol:** RECRUITER.")
@RequestMapping("/api/recruiters")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;

    public RecruiterProfileController(RecruiterProfileService recruiterProfileService) {
        this.recruiterProfileService = recruiterProfileService;
    }

    @GetMapping("/me")
    @Operation(summary = "Ver mi perfil", description = "Devuelve el perfil del reclutador con su empresa.")
    public RecruiterProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return recruiterProfileService.getMyProfile(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Editar mi perfil", description = "Actualiza el cargo (`jobTitle`). La empresa no se puede cambiar; sus datos se editan con `PUT /api/companies/{id}`.")
    public RecruiterProfileResponse updateMyProfile(@AuthenticationPrincipal User user,
                                                    @Valid @RequestBody RecruiterProfileRequest request) {
        return recruiterProfileService.update(user, request);
    }
}