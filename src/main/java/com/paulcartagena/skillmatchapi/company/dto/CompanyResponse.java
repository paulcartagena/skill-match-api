package com.paulcartagena.skillmatchapi.company.dto;

import com.paulcartagena.skillmatchapi.company.entity.Company;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Empresa.")
public class CompanyResponse {

    @Schema(description = "ID de la empresa.", example = "1")
    private Long id;
    @Schema(description = "Nombre de la empresa.", example = "TechNova")
    private String name;
    @Schema(description = "Sitio web.", example = "https://technova.dev")
    private String website;
    @Schema(description = "Industria.", example = "TECHNOLOGY")
    private Industry industry;
    @Schema(description = "Tamaño de la empresa.", example = "STARTUP")
    private CompanySize size;

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getWebsite(),
                company.getIndustry(),
                company.getSize()
        );
    }
}
