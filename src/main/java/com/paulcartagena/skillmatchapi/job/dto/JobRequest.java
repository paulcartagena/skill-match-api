package com.paulcartagena.skillmatchapi.job.dto;

import com.paulcartagena.skillmatchapi.job.enums.EmploymentType;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos de una oferta. En PUT se reemplaza la oferta completa: hay que enviar todos los campos.", example = """
        {
          "title": "Backend Developer Java",
          "description": "Desarrollo de APIs REST con Spring Boot y PostgreSQL.",
          "location": "San Salvador",
          "workMode": "REMOTE",
          "employmentType": "FULL_TIME",
          "skills": [{"skillId": 1, "required": true}, {"skillId": 2, "required": false}]
        }""")
public class JobRequest {

    @NotBlank(message = "Title is required.")
    @Schema(description = "Título de la oferta.", example = "Backend Developer Java")
    private String title;

    @NotBlank(message = "Description is required.")
    @Schema(description = "Descripción del puesto.", example = "Desarrollo de APIs REST con Spring Boot y PostgreSQL.")
    private String description;

    @Schema(description = "Ubicación. Opcional; en PUT, si no se envía, se borra.", example = "San Salvador")
    private String location;

    @NotNull(message = "Work mode is required.")
    @Schema(description = "Modalidad de trabajo.", example = "REMOTE")
    private WorkMode workMode;

    @NotNull(message = "Type is required.")
    @Schema(description = "Tipo de contrato.", example = "FULL_TIME")
    private EmploymentType employmentType;

    @NotEmpty(message = "At least one skill is needed.")
    @Schema(description = "Skills que pide la oferta. Al menos una requerida y sin repetir skillId. Las requeridas pesan 70 % del score de matching y las deseadas 30 %.")
    private List<@Valid JobSkillRequest> skills;
}