package com.paulcartagena.skillmatchapi.matching.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.matching.dto.CandidateMatchResponse;
import com.paulcartagena.skillmatchapi.matching.dto.JobMatchResponse;
import com.paulcartagena.skillmatchapi.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Matching", description = "Compatibilidad entre candidatos y ofertas, calculada al momento.\n\n**Score (0–100)** = 70 × cobertura de skills requeridas + 30 × cobertura de skills deseadas. Cada skill que coincide cuenta según el nivel del candidato: BEGINNER 0.4 · INTERMEDIATE 0.7 · ADVANCED 0.9 · EXPERT 1.0. Si la oferta no tiene skills deseadas, las requeridas valen el 100 %. No se excluye a nadie: los resultados vienen ordenados de mayor a menor score.")
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ofertas recomendadas para mí", description = "Todas las ofertas activas con el score del candidato autenticado, de mayor a menor. Incluye las skills que coinciden y las requeridas que le faltan.\n\n**Rol:** CANDIDATE.\n\n**Errores:** 404 todavía no completó su perfil.")
    public List<JobMatchResponse> getJobMatches(@AuthenticationPrincipal User user) {
        return matchingService.getJobMatches(user);
    }

    @GetMapping("/jobs/{jobId}/candidates")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Candidatos rankeados para mi oferta", description = "Todos los candidatos con su score para la oferta, de mayor a menor. Incluye las skills que coinciden y las requeridas que les faltan.\n\n**Rol:** RECRUITER dueño de la oferta.\n\n**Errores:** 403 no es el dueño · 404 oferta inexistente.")
    public List<CandidateMatchResponse> getCandidateMatches(@AuthenticationPrincipal User user,
                                                            @Parameter(description = "ID de la oferta.", example = "3") @PathVariable Long jobId) {
        return matchingService.getCandidateMatches(user, jobId);
    }
}
