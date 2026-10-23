package com.paulcartagena.skillmatchapi.auth.dto;

import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recruiters pick their company when registering, so the admin approving them also approves
 * which company they belong to. Send either companyId (join an existing company) or company (create a new one).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registro de reclutador. Envía **companyId** para unirte a una empresa existente (ver GET /api/companies) **o** **company** para crear una nueva, nunca los dos.", example = """
        {
          "email": "laura@technova.dev",
          "name": "Laura Méndez",
          "password": "secret123",
          "jobTitle": "Tech Recruiter",
          "company": {"name": "TechNova", "website": "https://technova.dev", "industry": "TECHNOLOGY", "size": "STARTUP"}
        }""")
public class RecruiterRegisterRequest {

    @NotBlank
    @Email(message = "Invalid format.")
    @Schema(description = "Email de la cuenta.", example = "laura@technova.dev")
    private String email;

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
    @Schema(description = "Nombre del reclutador, de 2 a 50 caracteres.", example = "Laura Méndez")
    private String name;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters.")
    @Schema(description = "Contraseña, de 6 a 72 caracteres.", example = "secret123")
    private String password;

    @Schema(description = "Cargo del reclutador. Opcional.", example = "Tech Recruiter")
    private String jobTitle;

    @Schema(description = "ID de una empresa existente para unirse a ella. No enviar junto con company.", nullable = true)
    private Long companyId;

    @Valid
    @Schema(description = "Datos de una empresa nueva para crearla. No enviar junto con companyId.", nullable = true)
    private CompanyRequest company;
}
