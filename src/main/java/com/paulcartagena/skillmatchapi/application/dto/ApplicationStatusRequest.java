package com.paulcartagena.skillmatchapi.application.dto;

import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusRequest {

    @NotNull(message = "Status is required.")
    @Schema(description = "Nuevo estado. Transiciones válidas: APPLIED → REVIEWED → ACCEPTED o REJECTED. ACCEPTED y REJECTED son finales.", example = "REVIEWED")
    private ApplicationStatus status;
}
