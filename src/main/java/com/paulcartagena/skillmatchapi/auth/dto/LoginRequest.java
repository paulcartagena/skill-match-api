package com.paulcartagena.skillmatchapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    @Email(message = "Invalid format.")
    private String email;

    @NotBlank
    private String password;
}