package com.paulcartagena.skillmatchapi.auth.dto;

import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
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
public class RecruiterRegisterRequest {

    @NotBlank
    @Email(message = "Invalid format.")
    private String email;

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
    private String name;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters.")
    private String password;

    private String jobTitle;

    private Long companyId;

    @Valid
    private CompanyRequest company;
}
