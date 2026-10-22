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

    // Intro shown at the top of /scalar, written for the frontend team
    private static final String DESCRIPTION = """
            Backend de una plataforma de empleabilidad que conecta **candidatos** con **ofertas de trabajo** \
            según la compatibilidad de sus skills.

            ## Primeros pasos

            1. Obtén un token con `POST /auth/login` (o `POST /auth/register` si eres candidato).
            2. En esta página, abre **Authentication**, elige **Bearer** y pega el `accessToken`.
            3. Prueba cualquier endpoint con **Test Request**.

            ## Roles

            | Rol | Cómo se obtiene | Qué puede hacer |
            |---|---|---|
            | **CANDIDATE** | `POST /auth/register` | Perfil, skills con nivel, ofertas recomendadas, postular |
            | **RECRUITER** | `POST /auth/register-recruiter` + aprobación de un admin | Publicar ofertas, ver candidatos rankeados, gestionar postulaciones |
            | **ADMIN** | Se crea al desplegar la API | Catálogo de skills, aprobar reclutadores |

            ## Flujos principales

            **Candidato:** `POST /auth/register` → `PUT /api/candidates/me` (completar perfil) → \
            `PUT /api/candidates/me/skills/{skillId}` (una vez por skill) → `GET /api/matching/jobs` → \
            `POST /api/jobs/{jobId}/applications` → `GET /api/applications/me`

            **Reclutador:** `POST /auth/register-recruiter` → *(aprobación del admin)* → `POST /auth/login` → \
            `POST /api/jobs` → `GET /api/matching/jobs/{jobId}/candidates` → `GET /api/jobs/{jobId}/applications` → \
            `PATCH /api/applications/{id}/status`

            ## Tokens

            - **Access token:** dura 15 minutos. Se envía en el header `Authorization: Bearer <accessToken>`.
            - **Refresh token:** dura 7 días. Cuando una llamada responda 401, pide un par nuevo con `POST /auth/refresh` \
            y guarda **los dos** tokens nuevos: cada login o refresh invalida el refresh token anterior del usuario.

            ## Errores

            Todos los errores tienen el mismo formato, y el `message` se puede mostrar al usuario:

            ```json
            { "status": 400, "message": "fullName: Full name is required.", "timestamp": "2026-10-22T18:30:00Z" }
            ```

            | Código | Significado |
            |---|---|
            | 400 | Datos inválidos o regla de negocio no cumplida |
            | 401 | Sin token, token vencido o credenciales incorrectas |
            | 403 | El rol no tiene permiso, o el recurso es de otro usuario |
            | 404 | El recurso no existe |
            | 409 | Conflicto: email ya registrado, postulación repetida, nombre en uso |
            | 429 | Demasiados intentos de login o registro (5 por minuto por IP); el header `Retry-After` indica cuánto esperar |

            ## En desarrollo

            Integración con un microservicio en **Python/FastAPI** para analizar CVs y hacer matching semántico de skills.
            """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Skill Match API")
                                .version("1.0")
                                .description(DESCRIPTION))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("Bearer Authentication"))
                .components(
                        new Components()
                                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Pega el accessToken de POST /auth/login.")));

    }
}
