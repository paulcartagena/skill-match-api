package com.paulcartagena.skillmatchapi.job.controller;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.job.dto.JobResponse;
import com.paulcartagena.skillmatchapi.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Jobs", description = "Ofertas de trabajo. Las publican los reclutadores; los candidatos ven las activas.")
@RequestMapping("api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Publicar oferta", description = "Crea una oferta activa a nombre de la empresa del reclutador.\n\nCada skill se marca como requerida (`required: true`, pesa 70 % del score de matching) o deseada (`false`, pesa 30 %). Debe haber al menos una requerida.\n\n**Rol:** RECRUITER.\n\n**Errores:** 400 datos inválidos, sin skill requerida, skill repetida o inexistente.")
    public JobResponse create(@AuthenticationPrincipal User user,
                              @Valid @RequestBody JobRequest request) {
        return jobService.create(user, request);
    }

    @GetMapping
    @Operation(summary = "Listar ofertas activas", description = "Todas las ofertas con estado ACTIVE.\n\n**Rol:** cualquier usuario autenticado.")
    public List<JobResponse> getJobs() {
        return jobService.getJobs();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Mis ofertas", description = "Todas las ofertas del reclutador autenticado, activas y cerradas.\n\n**Rol:** RECRUITER.")
    public List<JobResponse> getMyJobs(@AuthenticationPrincipal User user) {
        return jobService.getMyJobs(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver oferta", description = "Las ofertas cerradas solo las ve su reclutador; para los demás responden 404.\n\n**Rol:** cualquier usuario autenticado.\n\n**Errores:** 404 oferta inexistente o cerrada.")
    public JobResponse getJob(@AuthenticationPrincipal User user, @Parameter(description = "ID de la oferta.", example = "3") @PathVariable Long id) {
        return jobService.getJob(user, id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Editar oferta", description = "**Reemplaza la oferta completa**: hay que enviar todos los campos, incluso los que no cambian. Si no se envía `location`, se borra. Las skills que no se envíen se quitan.\n\n**Rol:** RECRUITER dueño de la oferta.\n\n**Errores:** 400 datos inválidos u oferta cerrada · 403 no es el dueño · 404 oferta inexistente.")
    public JobResponse update(@AuthenticationPrincipal User user,
                              @Parameter(description = "ID de la oferta.", example = "3") @PathVariable Long id,
                              @Valid @RequestBody JobRequest request) {
        return jobService.update(user, id, request);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Cerrar oferta", description = "Pasa la oferta a CLOSED: deja de recibir postulaciones y desaparece del listado y del matching. No se puede reabrir.\n\n**Rol:** RECRUITER dueño de la oferta.\n\n**Errores:** 400 ya estaba cerrada · 403 no es el dueño · 404 oferta inexistente.")
    public JobResponse close(@AuthenticationPrincipal User user, @Parameter(description = "ID de la oferta.", example = "3") @PathVariable Long id) {
        return jobService.close(user, id);
    }
}
