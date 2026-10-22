package com.paulcartagena.skillmatchapi.application.controller;

import com.paulcartagena.skillmatchapi.application.dto.ApplicationResponse;
import com.paulcartagena.skillmatchapi.application.dto.ApplicationStatusRequest;
import com.paulcartagena.skillmatchapi.application.service.ApplicationService;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Applications", description = "Postulaciones de candidatos a ofertas. Estados: APPLIED → REVIEWED → ACCEPTED o REJECTED.")
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Postular a una oferta", description = "El candidato autenticado postula; la postulación queda en APPLIED. Sin body.\n\n**Rol:** CANDIDATE (con perfil completo).\n\n**Errores:** 400 oferta cerrada · 404 oferta o perfil inexistente · 409 ya postuló a esta oferta.")
    public ApplicationResponse apply(@AuthenticationPrincipal User user, @Parameter(description = "ID de la oferta.", example = "3") @PathVariable Long jobId) {
        return applicationService.apply(user, jobId);
    }

    @GetMapping("/applications/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Mis postulaciones", description = "Postulaciones del candidato autenticado, con su estado.\n\n**Rol:** CANDIDATE.")
    public List<ApplicationResponse> getMyApplications(@AuthenticationPrincipal User user) {
        return applicationService.getMyApplications(user);
    }

    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Postulaciones de una oferta", description = "Candidatos que postularon a la oferta, con sus skills y el estado de cada postulación.\n\n**Rol:** RECRUITER dueño de la oferta.\n\n**Errores:** 403 no es el dueño · 404 oferta inexistente.")
    public List<ApplicationResponse> getJobApplications(@AuthenticationPrincipal User user,
                                                        @Parameter(description = "ID de la oferta.", example = "3") @PathVariable Long jobId) {
        return applicationService.getJobApplications(user, jobId);
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Cambiar estado de una postulación", description = "Transiciones válidas: APPLIED → REVIEWED → ACCEPTED o REJECTED. ACCEPTED y REJECTED son finales.\n\n**Rol:** RECRUITER dueño de la oferta.\n\n**Errores:** 400 transición inválida · 403 no es el dueño · 404 postulación inexistente.")
    public ApplicationResponse updateStatus(@AuthenticationPrincipal User user,
                                            @Parameter(description = "ID de la postulación.", example = "1") @PathVariable Long id,
                                            @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(user, id, request.getStatus());
    }
}
