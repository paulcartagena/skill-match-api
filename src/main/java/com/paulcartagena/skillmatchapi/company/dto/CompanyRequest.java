package com.paulcartagena.skillmatchapi.company.dto;

import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
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
public class CompanyRequest {

    @NotBlank(message = "Company name is required.")
    private String name;

    private String website;

    @NotNull(message = "Industry is required.")
    private Industry industry;

    @NotNull(message = "Company size is required.")
    private CompanySize size;
}
