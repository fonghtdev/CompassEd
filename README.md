# CompassEd

A learning platform with placement tests, personalized (AI-assisted) learning roadmaps, mini tests, final tests and paid study plans.

| Part | Technology | Directory |
|---|---|---|
| Backend | Spring Boot 4, Java 17, JWT, Flyway | `BE/compassed-api` |
| Frontend | Static HTML/JS served by Flask | `FE` |
| Database | MySQL 8 | `docker-compose.yml` |
| Integrations | Google OAuth, OpenAI, PayOS / VNPay, SMTP (Gmail) | environment variables |

## Quick start with Docker

```bash
cp .env.example .env     # fill in MYSQL_ROOT_PASSWORD, JWT_SECRET (>= 32 chars) and the other keys
docker compose up -d --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- MySQL: `localhost:3307`

Generate a `JWT_SECRET` with `openssl rand -hex 32`. The backend will not start without it.

## Run locally without Docker

Requires Java 17, Python 3 and MySQL 8.

```bash
# Backend
cd BE/compassed-api
export MYSQL_URL="jdbc:mysql://localhost:3306/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER=root MYSQL_PASSWORD=<password> JWT_SECRET=<string of >= 32 chars>
./mvnw spring-boot:run

# Frontend (another terminal)
cd FE
python3 -m pip install -r requirements.txt
python3 Extensions.py      # listens on port 3000 by default (override with FE_PORT); API_BASE is set in FE/config.js
```

On Windows use `scripts/*.ps1`: copy `scripts/dev-env.example.ps1` to `scripts/dev-env.ps1` (git-ignored), fill in the values, then run `scripts/run-all.ps1`.

## Environment variables

See `.env.example`. Keep every secret in `.env` or in real environment variables, and **never commit them**.

| Variable | Required | Notes |
|---|---|---|
| `JWT_SECRET` | Yes | >= 32 chars, no default value |
| `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD` | Yes | |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | No | If set, `DataSeeder` creates an admin account at startup |
| `OPENAI_API_KEY` | No | AI roadmap generation |
| `GOOGLE_CLIENT_ID` | No | Google sign-in |
| `MAIL_*` | No | Sends registration verification emails |
| `PAYOS_*` | No | PayOS payments (`PAYOS_ENABLED=true` to turn on) |

## Project layout

```
BE/compassed-api/   Spring Boot API (controller, service, repository, security)
  src/main/resources/db/migration/   SQL migrations (not applied automatically yet, see Technical notes)
FE/                 Static pages (template/, js/, css/) and Extensions.py (Flask)
scripts/            Windows (PowerShell) run scripts
docker-compose.yml  fe + be + mysql
```

## Tests

```bash
cd BE/compassed-api
JWT_SECRET=<string of >= 32 chars> ./mvnw test
```

On JDK 25 the older Lombok cannot compile the project: add `-Dlombok.version=1.18.42`.
`contextLoads` needs a running MySQL; `LocalFlowBusinessRulesTests` exercises the disabled local-mode code (`@Profile("removed-local")`) and currently fails.

## Security

- Create admin accounts through `ADMIN_EMAIL`/`ADMIN_PASSWORD` or SQL; no public endpoint grants the admin role.
- If a key ever leaks into git, revoke and reissue it immediately; deleting the file is not enough.

## Technical notes

- The schema is currently created by Hibernate (`ddl-auto: update`). Flyway does not run: on an empty MySQL a real start-up produced no `flyway_schema_history` table (suspected cause: Spring Boot 4 needs `spring-boot-starter-flyway`, not verified). The migration folder also contains two `V2__` files and two `V3__` files with duplicate versions. Clean up the migrations before enabling Flyway or switching to `ddl-auto: validate`.
- Payments currently use PayOS. The VNPay callback is disabled until `vnpay.hash-secret` is set.
