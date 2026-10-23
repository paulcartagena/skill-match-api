package com.paulcartagena.skillmatchapi.user.dto;

import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Perfil del reclutador.")
public class RecruiterProfileResponse {

    @Schema(description = "ID del perfil del reclutador.", example = "5")
    private Long id;
    @Schema(description = "Cargo del reclutador.", example = "Tech Recruiter")
    private String jobTitle;
    @Schema(description = "Empresa del reclutador.")
    private CompanyResponse company;
}
