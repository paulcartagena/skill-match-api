package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.user.dto.PendingRecruiterResponse;
import com.paulcartagena.skillmatchapi.user.service.AdminRecruiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recruiters")
@Tag(name = "Admin - Recruiters", description = "Aprobación de reclutadores. **Rol:** ADMIN.")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecruiterController {

    private final AdminRecruiterService adminRecruiterService;

    public AdminRecruiterController(AdminRecruiterService adminRecruiterService) {
        this.adminRecruiterService = adminRecruiterService;
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar reclutadores pendientes", description = "Reclutadores registrados que esperan aprobación, con la empresa a la que pertenecen.")
    public List<PendingRecruiterResponse> getPendingRecruiters() {
        return adminRecruiterService.getPendingRecruiters();
    }

    @PatchMapping("/{userId}/approve")
    @Operation(summary = "Aprobar reclutador", description = "Activa la cuenta: desde ese momento el reclutador puede iniciar sesión.\n\n**Errores:** 400 el usuario no es reclutador · 404 usuario inexistente.")
    public void approveRecruiter(@Parameter(description = "ID de usuario del reclutador (`userId` en la lista de pendientes).", example = "4") @PathVariable Long userId) {
        adminRecruiterService.approveRecruiter(userId);
    }
}
