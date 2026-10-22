# Skill Match API

API REST que conecta candidatos y ofertas de trabajo según sus skills.

**Producción:** https://34-202-95-184.sslip.io · **Docs:** [/scalar](https://34-202-95-184.sslip.io/scalar)

## Qué hace

- Registro y login con JWT para candidatos, reclutadores y admin.
- Candidatos con skills por nivel; ofertas con skills requeridas y deseadas.
- Matching: score de 0 a 100 entre candidato y oferta.
- Postulaciones con estados.
- Empresas con varios reclutadores, aprobados por un admin.

## Stack

Java 21 · Spring Boot 4 · PostgreSQL · Flyway · Docker · AWS (EC2 + RDS) · GitHub Actions

## Correr en local

Requiere Java 21 y PostgreSQL.

```bash
export SPRING_PROFILES_ACTIVE=dev
export LOCAL_DB_URL=jdbc:postgresql://localhost:5432/skill_match_db
export LOCAL_DB_USERNAME=postgres LOCAL_DB_PASSWORD=postgres
export JWT_SECRET=$(openssl rand -base64 48)
export ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=admin1234

./mvnw spring-boot:run
```

La API queda en http://localhost:8080, con la documentación en `/scalar`.

## Tests

```bash
./mvnw test
```

## Deploy

Cada merge a `master` corre los tests, publica la imagen en GHCR y la despliega en EC2.
