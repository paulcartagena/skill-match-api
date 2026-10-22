package com.paulcartagena.skillmatchapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos editables del perfil del reclutador. La empresa no se puede cambiar.")
public class RecruiterProfileRequest {

    @Schema(description = "Cargo del reclutador.", example = "Head of Talent")
    private String jobTitle;
}
