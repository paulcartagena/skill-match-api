package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Reclutador pendiente de aprobación.")
public class PendingRecruiterResponse {

    @Schema(description = "ID de usuario. Se usa en PATCH /api/admin/recruiters/{userId}/approve.", example = "4")
    private Long userId;
    @Schema(description = "Email del reclutador.", example = "laura@technova.dev")
    private String email;
    @Schema(description = "Cargo declarado al registrarse.", example = "Tech Recruiter")
    private String jobTitle;
    @Schema(description = "Empresa a la que pertenece, nueva o existente.")
    private CompanyResponse company;
}
