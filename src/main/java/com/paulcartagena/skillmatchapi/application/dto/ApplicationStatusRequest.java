package com.paulcartagena.skillmatchapi.application.dto;

import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusRequest {

    @NotNull(message = "Status is required.")
    private ApplicationStatus status;
}
