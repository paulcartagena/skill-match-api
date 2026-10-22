# Deploy en AWS (EC2 + RDS)

Guía paso a paso para publicar la API desde la consola de AWS.

```
Internet ──HTTPS──▶ EC2 t4g.micro (Ubuntu + Docker)
                     ├─ Caddy  (HTTPS automático con Let's Encrypt)
                     └─ API    (imagen ghcr.io/paulcartagena/skill-match-api)
                              │ red privada de AWS
                              ▼
                    RDS PostgreSQL db.t4g.micro (sin acceso público)
```

- **URL pública:** `https://<IP-con-guiones>.sslip.io` (por ejemplo `https://3-15-120-8.sslip.io`). [sslip.io](https://sslip.io) es un DNS gratis que resuelve ese nombre a tu IP, así que no hace falta comprar un dominio. Si después compras uno, solo cambia `API_DOMAIN`.
- **Deploy automático:** cada merge a `master` corre los tests, publica la imagen en GitHub Container Registry y la despliega en el EC2. Esto queda activo después del paso 8.
- **Costo estimado:** ~25 USD/mes en us-east-1 (EC2 ~6, IP pública ~4, discos ~4, RDS ~12). Es una estimación: revisa la calculadora de AWS.

> ⚠️ **Plan gratis de AWS:** la cuenta se cierra al cumplir 6 meses **o** al agotarse los créditos, lo que pase primero. Con 136 USD y ~25 USD/mes, los créditos duran unos **5 meses**. Crea la alerta del paso 0 y, antes de que se agoten, decide si pasas al plan de pago o apagas todo (paso 11).

---

## Antes de empezar

1. **Mergea los PRs pendientes**, sobre todo Flyway (#52) y la preparación para deploy (#53). Sin ellos, la API no arranca bien en producción.
2. Mergea este PR. El primer workflow en `master` publica la imagen. Revisa en **Actions** que haya terminado en verde.
3. **Haz pública la imagen:** en GitHub, ve a tu perfil → **Packages** → `skill-match-api` → **Package settings** → **Change visibility** → **Public**. Así el servidor puede descargarla sin credenciales.

Elige **una región** y usa siempre la misma en todos los pasos. `us-east-1` (N. Virginia) suele ser la más barata.

---

## 0. Alerta de gasto (2 min)

**Billing and Cost Management → Budgets → Create budget**
- Template: **Monthly cost budget**
- Monto: **25 USD**
- Email: el tuyo

AWS te avisa si el gasto mensual se acerca a ese monto. El saldo de créditos se ve en **Billing → Credits**.

## 1. Security groups (5 min)

**EC2 → Security Groups → Create security group** (en la VPC por defecto).

**`skill-match-api-sg`** (para el servidor)
| Tipo | Puerto | Origen | Para qué |
|---|---|---|---|
| HTTP | 80 | `0.0.0.0/0` | Let's Encrypt y redirección a HTTPS |
| HTTPS | 443 | `0.0.0.0/0` | La API |
| SSH | 22 | `0.0.0.0/0` | Deploy automático desde GitHub y tu acceso. Solo se entra con la clave privada; Ubuntu no permite contraseñas. |

**`skill-match-db-sg`** (para la base de datos)
| Tipo | Puerto | Origen |
|---|---|---|
| PostgreSQL | 5432 | el security group `skill-match-api-sg` (no una IP) |

Así, solo el servidor de la API puede conectarse a la base de datos.

## 2. Base de datos RDS (5 min de clics + ~10 min de espera)

**RDS → Create database**
- **Standard create**, engine **PostgreSQL**, versión **17.x**
- Template: **Free tier** (si aparece) o **Dev/Test**
- **DB instance identifier:** `skill-match-db`
- **Master username:** `skillmatch`
- **Password:** genera una fuerte y **guárdala**; la vas a usar en el paso 5
- **Instance class:** `db.t4g.micro`
- **Storage:** gp3, **20 GB**, sin autoscaling
- **Connectivity:**
  - **Public access: No**
  - VPC security group: elige **`skill-match-db-sg`** y quita `default`
- **Additional configuration:**
  - **Initial database name:** `skillmatch` ⚠️ si lo dejas vacío, la base no se crea y la API no arranca
  - Backups automáticos: déjalos activados (1–7 días)
  - Multi-AZ: **no** (duplica el costo)

Cuando quede en estado **Available**, copia el **Endpoint** que aparece en *Connectivity & security*, algo como `skill-match-db.xxxx.us-east-1.rds.amazonaws.com`.

## 3. Servidor EC2 (5 min)

**EC2 → Launch instance**
- **Name:** `skill-match-api`
- **AMI:** **Ubuntu Server 24.04 LTS**, arquitectura **64-bit (Arm)**
- **Instance type:** `t4g.micro`
- **Key pair:** créala (tipo ED25519, formato `.pem`) y **guarda el archivo**; lo vas a usar en el paso 8
- **Network settings:** *Select existing security group* → **`skill-match-api-sg`**
- **Storage:** 20 GB gp3

**IP fija:** **EC2 → Elastic IPs → Allocate Elastic IP address**, luego **Actions → Associate** con la instancia `skill-match-api`. Anota la IP; sin esto, la IP cambiaría al reiniciar el servidor y con ella la URL.

## 4. Preparar el servidor (5 min)

Conéctate desde la consola: **EC2 → Instances → `skill-match-api` → Connect → EC2 Instance Connect → Connect**. Se abre una terminal en el navegador.

```bash
curl -fsSL https://raw.githubusercontent.com/paulcartagena/skill-match-api/master/deploy/setup-ec2.sh | bash
```

El script instala Docker, agrega 1 GB de swap y descarga `docker-compose.yml`, `Caddyfile` y un `.env` vacío en `~/skill-match`.

Al terminar, **cierra la terminal y vuelve a conectarte** para que tu usuario pueda usar Docker.

## 5. Configurar los secretos (5 min)

Genera el secreto JWT:
```bash
openssl rand -base64 48
```

Edita el archivo de configuración:
```bash
nano ~/skill-match/.env
```

| Variable | Valor |
|---|---|
| `API_DOMAIN` | tu Elastic IP con guiones + `.sslip.io`, por ejemplo `3-15-120-8.sslip.io` |
| `PROD_DB_URL` | `jdbc:postgresql://<endpoint-del-paso-2>:5432/skillmatch?sslmode=require` |
| `PROD_DB_USERNAME` | `skillmatch` |
| `PROD_DB_PASSWORD` | la contraseña del paso 2 |
| `JWT_SECRET` | lo que imprimió `openssl` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | las credenciales del admin (mínimo 8 caracteres de contraseña) |
| `CORS_ALLOWED_ORIGINS` | déjalo como está hasta que el frontend tenga URL de producción |

Guarda con `Ctrl+O`, `Enter`, y sal con `Ctrl+X`.

## 6. Arrancar (2 min)

```bash
cd ~/skill-match
docker compose up -d
docker compose logs -f api
```

Espera a ver `Started SkillMatchApiApplication` y sal de los logs con `Ctrl+C`. En el primer arranque, Flyway crea las tablas y se crea el admin.

Comprueba desde tu navegador:
- `https://<API_DOMAIN>/actuator/health` → `{"status":"UP"}`
- `https://<API_DOMAIN>/scalar` → la documentación de la API

El certificado HTTPS tarda unos segundos la primera vez. Si falla, mira `docker compose logs caddy`.

## 7. Cargar las skills

La tabla de skills empieza vacía, y sin skills no se pueden crear jobs ni hacer matching. Desde `/scalar`:
1. `POST /auth/login` con el admin → copia el `accessToken`.
2. En **Authentication** de Scalar, pega el token.
3. `POST /api/skills` por cada skill, por ejemplo `{"name": "Java", "category": "TECHNICAL"}`.

## 8. Activar el deploy automático (3 min)

En GitHub: **repo → Settings → Secrets and variables → Actions → New repository secret**

| Secret | Valor |
|---|---|
| `EC2_HOST` | la Elastic IP |
| `EC2_USER` | `ubuntu` |
| `EC2_SSH_KEY` | el contenido completo del `.pem` del paso 3, incluidas las líneas `-----BEGIN…` y `-----END…` |

Desde ahí, cada merge a `master` corre los tests, publica la imagen y reinicia la API en el servidor. Mientras estos secrets no existan, el paso de deploy se salta solo.

## 9. Para el equipo de frontend

- **Base URL:** `https://<API_DOMAIN>`
- **Documentación interactiva:** `https://<API_DOMAIN>/scalar`
- **Autenticación:** `Authorization: Bearer <accessToken>`. El access token dura 15 min; se renueva con `POST /auth/refresh`.
- **CORS:** por ahora acepta `localhost:3000` y `localhost:5173`. Cuando el frontend tenga URL de producción, agrégala en `CORS_ALLOWED_ORIGINS` (separada por comas) y reinicia con `docker compose up -d`.

## 10. Operación del día a día

```bash
cd ~/skill-match
docker compose ps                 # estado
docker compose logs --tail 200 api
docker compose pull api && docker compose up -d   # deploy manual de la última imagen
```

Si cambias `docker-compose.yml` o `Caddyfile` en el repo, vuelve a descargarlos en el servidor, porque el deploy automático solo actualiza la imagen.

## 11. Apagar todo (cuando ya no se use)

Para que no se sigan consumiendo créditos:
1. **EC2:** terminate la instancia y **release** la Elastic IP. Una IP sin usar también se cobra.
2. **RDS:** delete la base. Puedes guardar un snapshot final, pero el snapshot también cuesta almacenamiento.
3. **Security groups:** puedes borrarlos; no cobran.
