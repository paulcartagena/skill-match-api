package com.paulcartagena.skillmatchapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Skill Match API")
                                .description(
                                        "REST backend for an AI-powered employability platform that matches " +
                                        "candidates to job openings based on skill compatibility. Built with Java 21, " +
                                        "Spring Boot, Spring Security, and JWT-based authentication (access + refresh " +
                                        "tokens). Integrates with a Python/FastAPI microservice for resume parsing " +
                                        "and semantic skill matching."))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("Bearer Authentication"))
                .components(
                        new Components()
                                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));

    }
}
