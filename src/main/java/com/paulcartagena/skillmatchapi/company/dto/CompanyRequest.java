package com.paulcartagena.skillmatchapi.company.dto;

import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de una empresa.")
public class CompanyRequest {

    @NotBlank(message = "Company name is required.")
    @Schema(description = "Nombre de la empresa. Único, sin distinguir mayúsculas.", example = "TechNova")
    private String name;

    @Schema(description = "Sitio web. Opcional.", example = "https://technova.dev")
    private String website;

    @NotNull(message = "Industry is required.")
    @Schema(description = "Industria.", example = "TECHNOLOGY")
    private Industry industry;

    @NotNull(message = "Company size is required.")
    @Schema(description = "Tamaño de la empresa.", example = "STARTUP")
    private CompanySize size;
}
