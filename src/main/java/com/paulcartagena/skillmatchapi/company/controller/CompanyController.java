package com.paulcartagena.skillmatchapi.company.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Companies", description = "Empresas de los reclutadores. Una empresa puede tener varios reclutadores.")
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Public so recruiters can find the companyId to join before registering
    @GetMapping
    @Operation(summary = "Listar empresas", description = "Lista pública, ordenada por nombre. Sirve para elegir el `companyId` al registrar un reclutador.")
    @SecurityRequirements
    public List<CompanyResponse> getCompanies() {
        return companyService.getCompanies();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver empresa", description = "**Errores:** 404 empresa inexistente.")
    @SecurityRequirements
    public CompanyResponse getCompany(@Parameter(description = "ID de la empresa.", example = "1") @PathVariable Long id) {
        return companyService.getCompany(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Editar empresa", description = "**Rol:** RECRUITER de esa empresa.\n\n**Errores:** 400 datos inválidos · 403 el reclutador es de otra empresa · 404 empresa inexistente · 409 nombre en uso.")
    public CompanyResponse update(@AuthenticationPrincipal User user,
                                  @Parameter(description = "ID de la empresa.", example = "1") @PathVariable Long id,
                                  @Valid @RequestBody CompanyRequest request) {
        return companyService.update(user, id, request);
    }
}
